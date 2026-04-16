package com.example.taximotoapp_backend.trajet.service;

import com.example.taximotoapp_backend.User.model.Chauffeur;
import com.example.taximotoapp_backend.User.model.Client;
import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.User.repository.ChauffeurRepository;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.model.enumClass.Availability;
import com.example.taximotoapp_backend.model.enumClass.TripStatus;
import com.example.taximotoapp_backend.trajet.dto.request.TrajetRequest;
import com.example.taximotoapp_backend.trajet.mapper.TrajetMapper;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import com.example.taximotoapp_backend.trajet.model.TrajetLocation;
import com.example.taximotoapp_backend.trajet.repository.TrajetRepository;
import com.example.taximotoapp_backend.trajet.dto.response.TrajetResponse;
import com.example.taximotoapp_backend.websocket.service.TimeoutService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TrajetService {
    private final TrajetRepository trajetRepository;
    private final UserRepository userRepository;
    private final TrajetMapper trajetMapper;
    private final ChauffeurRepository chauffeurRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final TimeoutService timeoutService;
    private final com.example.taximotoapp_backend.location.repository.LocationRepository locationRepository;
    private final MapboxService mapboxService;

    public TrajetResponse createTrajet(TrajetRequest trajetRequest){
        // recuperer user a travers le jwt
        String email= SecurityContextHolder.getContext().getAuthentication().getName();
        User client = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("client not found"));

        // mapping request -> entity
        Trajet trajet = trajetMapper.toEntity(trajetRequest);

        // --- Initialiser/Mettre à jour la position du client dans la base ---
        com.example.taximotoapp_backend.location.model.Location clientLocation = client.getLocation();
        if (clientLocation == null) {
            clientLocation = new com.example.taximotoapp_backend.location.model.Location();
            clientLocation.setUser(client);
            client.setLocation(clientLocation);
        }
        clientLocation.setLatitude(trajetRequest.getPickupLatitude());
        clientLocation.setLongitude(trajetRequest.getPickupLongitude());
        locationRepository.save(clientLocation);

        // créer et attacher TrajetLocation
        TrajetLocation trajetLocation = new TrajetLocation();
        trajetLocation.setPickupLatitude(trajetRequest.getPickupLatitude());
        trajetLocation.setPickupLongitude(trajetRequest.getPickupLongitude());
        trajetLocation.setDestinationLatitude(trajetRequest.getDestinationLatitude());
        trajetLocation.setDestinationLongitude(trajetRequest.getDestinationLongitude());

        // --- Road Data from Mapbox ---
        MapboxService.RouteDetails roadmap = mapboxService.getRouteDetails(
                trajetRequest.getPickupLatitude(), trajetRequest.getPickupLongitude(),
                trajetRequest.getDestinationLatitude(), trajetRequest.getDestinationLongitude()
        );

        // --- Address Resolution ---
        String pAddress = trajetRequest.getPickupAddress();
        if (pAddress == null || pAddress.isEmpty()) {
            pAddress = mapboxService.reverseGeocode(
                    trajetRequest.getPickupLatitude(), trajetRequest.getPickupLongitude());
        }
        trajetLocation.setPickupAddress(pAddress);

        String dAddress = trajetRequest.getDestinationAddress();
        if (dAddress == null || dAddress.isEmpty()) {
            dAddress = mapboxService.reverseGeocode(
                    trajetRequest.getDestinationLatitude(), trajetRequest.getDestinationLongitude());
        }
        trajetLocation.setDestinationAddress(dAddress);
        trajetLocation.setEncodedPolyline(roadmap.getEncodedPolyline());

        trajetLocation.setTrajet(trajet);
        trajet.setTrajetLocation(trajetLocation);

        trajet.setClient((Client) client);
        trajet.setChauffeur(null);
        trajet.setDistanceKm(roadmap.getDistanceKm());
        trajet.setPrice(calculatePrice(roadmap.getDistanceKm()));
        trajet.setStatus(TripStatus.Created);
        trajet.setRequestedAt(LocalDateTime.now());

        Trajet saved = trajetRepository.save(trajet);

        // trouver chauffeurs
        List<Chauffeur> drivers = findDriversWithExpansion(
                trajetRequest.getPickupLatitude(),
                trajetRequest.getPickupLongitude()
        );

        if (drivers.isEmpty()) {
            throw new RuntimeException("Aucun chauffeur disponible");
        }

        sendTrajetToDrivers(saved, drivers);
        timeoutService.handleTimeout(saved.getId());
        return trajetMapper.toDTO(saved);
    }

    private Double calculatePrice(Double distanceKm) {
        final double BASE_FARE = 0.7;
        final double PER_KM_RATE = 0.8;
        return BASE_FARE + (distanceKm * PER_KM_RATE);
    }

    public void sendTrajetToDrivers(Trajet trajet, List<Chauffeur> drivers) {
        for (Chauffeur driver : drivers) {
            messagingTemplate.convertAndSend(
                    "/topic/driver/" + driver.getId(),
                    trajetMapper.toDTO(trajet)
            );
        }
    }

    public List<Chauffeur> findDriversWithExpansion(double lat, double lon) {
        Set<Chauffeur> driversSet = new LinkedHashSet<>();
        for (int i = 1; i < 10; i += 2) {
            List<Chauffeur> drivers = chauffeurRepository.findNearbyDrivers(lat, lon, i);
            driversSet.addAll(drivers);
            if (driversSet.size() >= 3) {
                return driversSet.stream().limit(3).toList();
            }
        }
        return new ArrayList<>(driversSet);
    }

    @Transactional
    public synchronized void handleDriverResponse(Long trajetId, String action) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Chauffeur chauffeur = (Chauffeur) userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Chauffeur not found"));

        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new RuntimeException("Trajet not found"));

        if (trajet.getStatus() != TripStatus.Created) {
            throw new RuntimeException("Trajet déjà pris");
        }

        if (action.equalsIgnoreCase("ACCEPT")) {
            trajet.setChauffeur(chauffeur);
            trajet.setStatus(TripStatus.Accepted);
            trajetRepository.save(trajet);

            Map<String, Object> driverDetails = new HashMap<>();
            driverDetails.put("status", "ACCEPTED");
            driverDetails.put("driverId", chauffeur.getId());
            driverDetails.put("driverName", chauffeur.getFullName());
            driverDetails.put("driverPhoto", chauffeur.getPhotoUrl() != null ? chauffeur.getPhotoUrl() : "");
            driverDetails.put("driverRating", chauffeur.getRating() != null ? chauffeur.getRating() : 0.0);
            driverDetails.put("vehicleModel", chauffeur.getVehicleModel() != null ? chauffeur.getVehicleModel() : "Moto-Taxi");
            driverDetails.put("vehiclePlate", chauffeur.getVehiclePlate() != null ? chauffeur.getVehiclePlate() : "");

            messagingTemplate.convertAndSend(
                    "/topic/client/" + trajet.getClient().getId(),
                    driverDetails
            );

            messagingTemplate.convertAndSend(
                    "/topic/trajet/" + trajetId,
                    Map.of("status", "TAKEN", "trajetId", trajetId)
            );
        }
    }

    public TrajetResponse startTrajet(Long trajetId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User chauffeur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("chauffeur not found"));

        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new RuntimeException("trajet not found"));

        if (trajet.getStatus() != TripStatus.Accepted) {
            throw new RuntimeException("Trajet déjà pris ou non disponible");
        }
        trajet.setStartedAt(LocalDateTime.now());
        trajet.setStatus(TripStatus.Started);
        Trajet saved = trajetRepository.save(trajet);

        messagingTemplate.convertAndSend("/topic/client/" + trajet.getClient().getId(),
                Map.of("status", "STARTED", "trajetId", trajetId, "message", "Trip started!"));

        return trajetMapper.toDTO(saved);
    }

    public TrajetResponse terminerTrajet(Long trajetId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User chauffeur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("chauffeur not found"));

        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new RuntimeException("trajet not found"));

        if (trajet.getChauffeur() == null || !trajet.getChauffeur().getId().equals(chauffeur.getId())) {
            throw new RuntimeException("Ce trajet ne t'appartient pas");
        }

        if (trajet.getStatus() != TripStatus.Started) {
            throw new RuntimeException("Le trajet n'est pas en cours");
        }
        trajet.setStatus(TripStatus.Completed);
        trajet.setCompletedAt(LocalDateTime.now());
        Trajet saved = trajetRepository.save(trajet);

        messagingTemplate.convertAndSend("/topic/client/" + trajet.getClient().getId(),
                Map.of("status", "COMPLETED", "trajetId", trajetId, "message", "Trip completed!"));

        return trajetMapper.toDTO(saved);
    }

    public TrajetResponse annulerTrajet(Long trajetId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("client not found"));

        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new RuntimeException("trajet not found"));

        if (!trajet.getClient().getId().equals(client.getId())) {
            throw new RuntimeException("Ce trajet ne t'appartient pas");
        }

        if (trajet.getStatus() == TripStatus.Completed) {
            throw new RuntimeException("Impossible d'annuler un trajet terminé");
        }
        trajet.setStatus(TripStatus.Canceled);

        Trajet saved = trajetRepository.save(trajet);
        messagingTemplate.convertAndSend("/topic/trajet/" + trajetId,
                Map.of("status", "CANCELLED", "trajetId", trajetId));

        return trajetMapper.toDTO(saved);
    }

    public TrajetResponse getTrajetById(Long id){
        String email=SecurityContextHolder.getContext().getAuthentication().getName();
        User user=userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Trajet trajet = trajetRepository.findById(id).orElseThrow(() -> new RuntimeException("trajet not found"));

        Boolean isClient=trajet.getClient().getId().equals(user.getId());
        Boolean isChauffeur=trajet.getChauffeur()!=null && trajet.getChauffeur().getId().equals(user.getId());
        if (!isChauffeur && !isClient){
            throw new RuntimeException("Access denied");
        }
        return trajetMapper.toDTO(trajet);
    }

    public List<TrajetResponse> getAvailableTrajetsForDriver() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Chauffeur chauffeur = chauffeurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Chauffeur not found"));

        if (chauffeur.getAvailability() == Availability.FALSE) {
            return Collections.emptyList();
        }

        if (chauffeur.getLocation() == null) {
            return Collections.emptyList();
        }

        double lat = chauffeur.getLocation().getLatitude();
        double lon = chauffeur.getLocation().getLongitude();

        Set<Trajet> trajetsSet = new LinkedHashSet<>();

        for (int i = 1; i < 10; i += 2) {
            List<Trajet> found = trajetRepository.findNearbyAvailableTrajets(lat, lon, i);
            trajetsSet.addAll(found);
            if (trajetsSet.size() >= 5) {
                break;
            }
        }
        return trajetsSet.stream()
                .limit(5)
                .map(trajetMapper::toDTO)
                .toList();
    }

    public TrajetResponse driverArrivedAtPickup(Long trajetId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Chauffeur chauffeur = chauffeurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Chauffeur not found"));

        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new RuntimeException("Trajet not found"));

        if (!trajet.getChauffeur().getId().equals(chauffeur.getId())) {
            throw new RuntimeException("Ce trajet ne t'appartient pas");
        }

        messagingTemplate.convertAndSend(
                "/topic/client/" + trajet.getClient().getId(),
                Map.of("status", "ARRIVED", "trajetId", trajetId, "message", "Your driver is here")
        );

        return trajetMapper.toDTO(trajet);
    }

    public List<TrajetResponse> getClientHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User client = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Client not found"));
        return trajetRepository.findByClientIdOrderByRequestedAtDesc(client.getId())
                .stream()
                .map(trajetMapper::toDTO)
                .toList();
    }

    public List<TrajetResponse> getDriverHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User driver = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Driver not found"));
        return trajetRepository.findByChauffeurIdOrderByRequestedAtDesc(driver.getId())
                .stream()
                .map(trajetMapper::toDTO)
                .toList();
    }
}