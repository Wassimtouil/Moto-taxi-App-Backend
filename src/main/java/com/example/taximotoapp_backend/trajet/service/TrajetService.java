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
import com.example.taximotoapp_backend.paiement.service.PaiementService;
import com.example.taximotoapp_backend.trajet.dto.response.TrajetResponse;
import com.example.taximotoapp_backend.websocket.service.TimeoutService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;

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
    private final PaiementService paiementService;

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
        // Determine initial status: Scheduled if far in future, Created if immediate or starting soon
        TripStatus initialStatus = TripStatus.Created;
        if (trajetRequest.getScheduledAt() != null) {
            trajet.setScheduledAt(trajetRequest.getScheduledAt());
            LocalDateTime now = LocalDateTime.now();
            // If scheduled more than 10 minutes away, use 'Scheduled' status (delayed dispatch)
            if (trajetRequest.getScheduledAt().isAfter(now.plusMinutes(10))) {
                initialStatus = TripStatus.Scheduled;
            }
        }
        trajet.setStatus(initialStatus);
        trajet.setRequestedAt(LocalDateTime.now());
        trajet.setScheduledAt(trajetRequest.getScheduledAt());
        trajet.setPreferredDriverGender(trajetRequest.getPreferredDriverGender());
        trajet.setPreferredDriverId(trajetRequest.getPreferredDriverId());
        trajet.setPaymentMethod(trajetRequest.getPaymentMethod() != null ? trajetRequest.getPaymentMethod() : com.example.taximotoapp_backend.model.enumClass.PaiementType.CASH);

        Trajet saved = trajetRepository.save(trajet);
        System.out.println("🔍 [DB SAVE] Ride ID: " + saved.getId() + " Status: " + saved.getStatus() + " ScheduledAt: " + saved.getScheduledAt());
        System.out.println("🔍 [DB SAVE] Server Local Time is: " + LocalDateTime.now());

        // If it's a far-future scheduled ride, we don't dispatch immediately
        if (saved.getStatus() == TripStatus.Scheduled) {
            System.out.println("📅 Ride is far in the future. Waiting for scheduler.");
            TrajetResponse response = trajetMapper.toDTO(saved);
            if (saved.getScheduledAt() != null && response.getScheduledAt() == null) {
                response.setScheduledAt(saved.getScheduledAt());
            }
            return response;
        }

        System.out.println("⚡ Ride is starting SOON or NOW. Searching for drivers...");
        System.out.println("🎯 [DEBUG] preferredDriverId from request: " + trajetRequest.getPreferredDriverId());
        // trouver chauffeurs (Immediate ride)
        List<Chauffeur> drivers;

        if (trajetRequest.getPreferredDriverId() != null) {
            System.out.println("🎯 Rider explicitly requested driver ID: " + trajetRequest.getPreferredDriverId());
            Chauffeur preferredDriver = chauffeurRepository.findById(trajetRequest.getPreferredDriverId())
                    .orElseThrow(() -> new RuntimeException("Le chauffeur sélectionné n'existe pas"));

            if (preferredDriver.getAvailability() != com.example.taximotoapp_backend.model.enumClass.Availability.TRUE) {
                throw new RuntimeException("Le chauffeur sélectionné n'est plus disponible");
            }
            drivers = List.of(preferredDriver);
        } else {
            drivers = findDriversWithExpansion(
                    trajetRequest.getPickupLatitude(),
                    trajetRequest.getPickupLongitude(),
                    trajetRequest.getPreferredDriverGender()
            );
        }

        System.out.println("🚗 Found " + drivers.size() + " nearby available drivers.");

        if (drivers.isEmpty()) {
            System.out.println("❌ No drivers found for the ride.");
            throw new RuntimeException("Aucun chauffeur disponible");
        }

        System.out.println("📡 Dispatching ride to found drivers...");
        sendTrajetToDrivers(saved, drivers);

        // ONLY trigger the 15-second timeout for IMMEDIATE rides.
        // Scheduled rides (even if dispatched early) should stay alive until their departure time.
        if (saved.getScheduledAt() == null) {
            timeoutService.handleTimeout(saved.getId());
        } else {
            System.out.println("⏳ Scheduled ride detected. Persistent until departure time (no 15s timeout).");
        }

        return trajetMapper.toDTO(saved);
    }

    private Double calculatePrice(Double distanceKm) {
        final double BASE_FARE = 0.7;
        final double PER_KM_RATE = 0.8;
        return BASE_FARE + (distanceKm * PER_KM_RATE);
    }

    public void sendTrajetToDrivers(Trajet trajet, List<Chauffeur> drivers) {
        TrajetResponse dto = trajetMapper.toDTO(trajet);
        // Force-set scheduledAt in case MapStruct's generated code is stale
        if (trajet.getScheduledAt() != null && dto.getScheduledAt() == null) {
            dto.setScheduledAt(trajet.getScheduledAt());
            System.out.println("⚠️ [BROADCAST] MapStruct missed scheduledAt — manually set it!");
        }
        System.out.println("📡 [BROADCAST] Preparing to send Trajet " + trajet.getId() + " to " + drivers.size() + " drivers.");
        System.out.println("   -> scheduledAt on Entity: " + trajet.getScheduledAt());
        System.out.println("   -> scheduledAt on DTO: " + dto.getScheduledAt());
        System.out.println("   -> Status: " + dto.getStatus());

        for (Chauffeur driver : drivers) {
            String destination = "/topic/driver/" + driver.getId();
            try {
                messagingTemplate.convertAndSend(destination, dto);
                System.out.println("✅ [BROADCAST] Message sent successfully to " + destination);
            } catch (Exception e) {
                System.err.println("❌ [BROADCAST] Error sending to " + destination + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    public List<Chauffeur> findDriversWithExpansion(double lat, double lon, String preferredGender) {
        Set<Chauffeur> driversSet = new LinkedHashSet<>();
        for (int i = 1; i < 10; i += 2) {
            List<Chauffeur> drivers;
            if (preferredGender != null && !preferredGender.isEmpty()) {
                drivers = chauffeurRepository.findNearbyDriversByGender(lat, lon, i, preferredGender);
            } else {
                drivers = chauffeurRepository.findNearbyDrivers(lat, lon, i);
            }
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

            messagingTemplate.convertAndSend(
                    "/topic/client/" + trajet.getClient().getId(),
                    buildDriverDetailsMap(chauffeur, trajet)
            );

            messagingTemplate.convertAndSend(
                    "/topic/trajet/" + trajetId,
                    Map.of("status", "TAKEN", "trajetId", trajetId)
            );
        }
    }

    private Map<String, Object> buildDriverDetailsMap(Chauffeur chauffeur, LocalDateTime scheduledAt) {
        Map<String, Object> driverDetails = new HashMap<>();
        driverDetails.put("status", "ACCEPTED");
        driverDetails.put("driverId", chauffeur.getId());
        driverDetails.put("driverName", chauffeur.getFullName());
        driverDetails.put("driverPhoto", chauffeur.getPhotoUrl() != null ? chauffeur.getPhotoUrl() : "");
        driverDetails.put("driverRating", chauffeur.getNoteMoyenne() != null ? chauffeur.getNoteMoyenne() : 0.0);
        driverDetails.put("vehicleModel", chauffeur.getVehicleModel() != null ? chauffeur.getVehicleModel() : "Moto-Taxi");
        driverDetails.put("vehiclePlate", chauffeur.getVehiclePlate() != null ? chauffeur.getVehiclePlate() : "");
        if (scheduledAt != null) {
            driverDetails.put("scheduledAt", scheduledAt.toString());
        }
        if (chauffeur.getTrajets() != null && !chauffeur.getTrajets().isEmpty()) {
            // Get the last trajet to determine payment method if needed,
            // but better to pass Trajet object directly to this method.
            // For now, let's just make sure handleDriverResponse passes it or we fetch it.
        }
        return driverDetails;
    }

    private Map<String, Object> buildDriverDetailsMap(Chauffeur chauffeur, Trajet trajet) {
        Map<String, Object> driverDetails = buildDriverDetailsMap(chauffeur, trajet.getScheduledAt());
        driverDetails.put("paymentMethod", trajet.getPaymentMethod());
        return driverDetails;
    }

    @Transactional
    public TrajetResponse startTrajet(Long trajetId) {
        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new RuntimeException("trajet not found"));

        if (trajet.getStatus() != TripStatus.Accepted && trajet.getStatus() != TripStatus.Arrived) {
            throw new RuntimeException("Trajet déjà pris ou non disponible (status=" + trajet.getStatus() + ")");
        }
        trajet.setStartedAt(LocalDateTime.now());
        trajet.setStatus(TripStatus.Started);
        Trajet saved = trajetRepository.save(trajet);

        // User instruction: Create payment and mark as PAYE when trip starts
        paiementService.createFromTrajet(saved);

        messagingTemplate.convertAndSend("/topic/client/" + trajet.getClient().getId(),
                Map.of("status", "STARTED", "trajetId", trajetId, "message", "Trip started!", "paymentMethod", saved.getPaymentMethod()));
        messagingTemplate.convertAndSend("/topic/trajet/" + trajetId,
                Map.of("status", "STARTED", "trajetId", trajetId, "paymentMethod", saved.getPaymentMethod()));

        return trajetMapper.toDTO(saved);
    }

    @Transactional
    public TrajetResponse terminerTrajet(Long trajetId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("chauffeur not found"));

        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new RuntimeException("trajet not found"));

        if (trajet.getChauffeur() == null || !trajet.getChauffeur().getId().equals(user.getId())) {
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
        messagingTemplate.convertAndSend("/topic/trajet/" + trajetId,
                Map.of("status", "COMPLETED", "trajetId", trajetId));

        return trajetMapper.toDTO(saved);
    }

    @Transactional
    public TrajetResponse annulerTrajet(Long trajetId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("user not found"));

        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new RuntimeException("trajet not found"));

        boolean isClient = trajet.getClient().getId().equals(user.getId());
        boolean isChauffeur = trajet.getChauffeur() != null && trajet.getChauffeur().getId().equals(user.getId());

        if (!isClient && !isChauffeur) {
            throw new RuntimeException("Ce trajet ne t'appartient pas");
        }

        if (trajet.getStatus() == TripStatus.Completed) {
            throw new RuntimeException("Impossible d'annuler un trajet terminé");
        }
        trajet.setStatus(TripStatus.Canceled);

        Trajet saved = trajetRepository.save(trajet);
        String cancelledBy = isClient ? "CLIENT" : "CHAUFFEUR";

        messagingTemplate.convertAndSend("/topic/trajet/" + trajetId,
                Map.of("status", "CANCELLED", "trajetId", trajetId, "cancelledBy", cancelledBy));
        messagingTemplate.convertAndSend("/topic/client/" + trajet.getClient().getId(),
                Map.of("status", "CANCELLED", "trajetId", trajetId, "cancelledBy", cancelledBy));

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
            List<Trajet> found = trajetRepository.findNearbyAvailableTrajets(lat, lon, i, chauffeur.getId());
            trajetsSet.addAll(found);
            if (trajetsSet.size() >= 5) {
                break;
            }
        }
        return trajetsSet.stream()
                .limit(5)
                .map(trajet -> {
                    TrajetResponse dto = trajetMapper.toDTO(trajet);
                    // Force-set scheduledAt in case MapStruct's generated code is stale
                    if (trajet.getScheduledAt() != null && dto.getScheduledAt() == null) {
                        dto.setScheduledAt(trajet.getScheduledAt());
                    }
                    return dto;
                })
                .toList();
    }

    @Transactional
    public TrajetResponse driverArrivedAtPickup(Long trajetId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Chauffeur chauffeur = chauffeurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Chauffeur not found"));

        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new RuntimeException("Trajet not found"));

        if (!trajet.getChauffeur().getId().equals(chauffeur.getId())) {
            throw new RuntimeException("Ce trajet ne t'appartient pas");
        }

        trajet.setStatus(TripStatus.Arrived);
        Trajet saved = trajetRepository.save(trajet);

        String destination = "/topic/client/" + trajet.getClient().getId();
        System.out.println("📡 Sending ARRIVED notification to: " + destination);
        messagingTemplate.convertAndSend(
                destination,
                Map.of("status", "ARRIVED", "trajetId", trajetId, "message", "Your driver is here")
        );
        messagingTemplate.convertAndSend("/topic/trajet/" + trajetId,
                Map.of("status", "ARRIVED", "trajetId", trajetId));

        return trajetMapper.toDTO(saved);
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

    @Transactional
    public Optional<TrajetResponse> getActiveTrajetForDriver() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User driver = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Driver not found"));
        List<Trajet> active = trajetRepository.findActiveTrajetByChauffeurId(driver.getId());
        if (active.isEmpty()) return Optional.empty();
        return Optional.of(trajetMapper.toDTO(active.get(0)));
    }

    @Transactional
    public Optional<TrajetResponse> getActiveTrajetForClient() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User client = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Client not found"));
        List<Trajet> active = trajetRepository.findActiveTrajetByClientId(client.getId());
        if (active.isEmpty()) return Optional.empty();
        return Optional.of(trajetMapper.toDTO(active.get(0)));
    }

    @Transactional
    @Scheduled(fixedRate = 5000) // Every 5 seconds (Optimized from 30s)
    public void checkUpcomingScheduledRides() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("⏰ [SCHEDULER] Running at: " + now);

        // --- Phase 1: Triggering Upcoming Rides (now + 10 mins) ---
        LocalDateTime limit = now.plusMinutes(10);
        List<Trajet> upcoming = trajetRepository.findUpcomingScheduledTrajets(limit);

        if (upcoming.isEmpty()) {
            System.out.println("📝 [SCHEDULER] No rides matching query (status='Scheduled' AND scheduledAt <= " + limit + ")");
            // Debug: Check if there are ANY scheduled rides at all
            List<Trajet> allScheduled = trajetRepository.findByStatus(TripStatus.Scheduled);
            if (!allScheduled.isEmpty()) {
                System.out.println("📝 [SCHEDULER DEBUG] Found " + allScheduled.size() + " total 'Scheduled' rides in DB:");
                for (Trajet s : allScheduled) {
                    System.out.println("   -> ID: " + s.getId() + " is for: " + s.getScheduledAt());
                }
            }
        } else {
            System.out.println("📝 [SCHEDULER] Found " + upcoming.size() + " rides to trigger.");
        }

        for (Trajet t : upcoming) {
            System.out.println("🚀 [SCHEDULER] Triggering ride ID " + t.getId());

            double lat = 0, lon = 0;
            if (t.getTrajetLocation() != null) {
                lat = t.getTrajetLocation().getPickupLatitude();
                lon = t.getTrajetLocation().getPickupLongitude();
            }
            System.out.println("📍 [SCHEDULER] Pickup Location: " + lat + ", " + lon);

            t.setStatus(TripStatus.Created);
            trajetRepository.save(t);

            List<Chauffeur> drivers = findDriversWithExpansion(lat, lon, t.getPreferredDriverGender());
            System.out.println("🚗 [SCHEDULER] Found " + drivers.size() + " drivers for ride " + t.getId());

            if (!drivers.isEmpty()) {
                System.out.println("📡 [SCHEDULER] Dispatching to WS...");
                sendTrajetToDrivers(t, drivers);
            }
        }

        // --- Phase 2: Expiring Missed Rides (time has passed) ---
        List<Trajet> expired = trajetRepository.findExpiredScheduledTrajets(now);
        for (Trajet e : expired) {
            System.out.println("⚠️ [SCHEDULER] Expiring ride ID " + e.getId() + " (Time reached, no driver)");

            e.setStatus(TripStatus.Canceled);
            trajetRepository.save(e);

            // Notify Rider
            messagingTemplate.convertAndSend(
                    "/topic/client/" + e.getClient().getId(),
                    Map.of("status", "NO_DRIVER", "message", "Votre trajet planifié a été annulé car aucun chauffeur n'est disponible.", "trajetId", e.getId())
            );

            // Notify Drivers to remove card
            messagingTemplate.convertAndSend(
                    "/topic/trajet/" + e.getId(),
                    Map.of("status", "TIMEOUT", "trajetId", e.getId())
            );
        }
    }
}
