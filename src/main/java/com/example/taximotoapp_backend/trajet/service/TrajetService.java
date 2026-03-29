package com.example.taximotoapp_backend.trajet.service;

import com.example.taximotoapp_backend.User.model.Chauffeur;
import com.example.taximotoapp_backend.User.model.Client;
import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.User.repository.ChauffeurRepository;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.model.enumClass.TripStatus;
import com.example.taximotoapp_backend.trajet.dto.TrajetRequest;
import com.example.taximotoapp_backend.trajet.mapper.TrajetMapper;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import com.example.taximotoapp_backend.trajet.repository.TrajetRepository;
import com.example.taximotoapp_backend.trajet.response.TrajetResponse;
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
    public TrajetResponse createTrajet(TrajetRequest trajetRequest){
        // recuperer user a travers le jwt
        String email= SecurityContextHolder.getContext().getAuthentication().getName();
        User client = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("client not found"));
        //mapping request -> entity
        Trajet trajet = trajetMapper.toEntity(trajetRequest);
        // calcul distance
        double distance = calculateDistance(
                trajetRequest.getPickupLatitude(),
                trajetRequest.getPickupLongitude(),
                trajetRequest.getDestinationLatitude(),
                trajetRequest.getDestinationLongitude()
        );
        trajet.setClient((Client) client);
        trajet.setChauffeur(null);
        trajet.setDistanceKm(distance);
        trajet.setPrice(1000.0);
        trajet.setStatus(TripStatus.Created);
        trajet.setRequestedAt(LocalDateTime.now());

        Trajet saved=trajetRepository.save(trajet);

        // trouver chauffeurs (5km → 10km)
        List<Chauffeur> drivers = findDriversWithExpansion(
                trajetRequest.getPickupLatitude(),
                trajetRequest.getPickupLongitude()
        );

        if (drivers.isEmpty()) {
            throw new RuntimeException("Aucun chauffeur disponible");
        }

        //envoyer demande via WebSocket
        sendTrajetToDrivers(saved, drivers);

        //lancer timer
        timeoutService.handleTimeout(saved.getId());
        return trajetMapper.toDTO(saved);
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
        // récupérer chauffeur connecté depuis JWT
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

            // notifier client
            messagingTemplate.convertAndSend(
                    "/topic/client/" + trajet.getClient().getId(),
                    "Trajet accepté par chauffeur " + chauffeur.getId()
            );

            // notifier autres chauffeurs
            messagingTemplate.convertAndSend(
                    "/topic/trajet/" + trajetId,
                    "Trajet déjà pris"
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
        trajet.setChauffeur((Chauffeur) chauffeur);
        trajet.setStatus(TripStatus.Started);
        return trajetMapper.toDTO(trajetRepository.save(trajet));
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
        return trajetMapper.toDTO(trajetRepository.save(trajet));
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
        return trajetMapper.toDTO(trajetRepository.save(trajet));
    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2)
                * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public TrajetResponse getTrajetById(Long id){
        String email=SecurityContextHolder.getContext().getAuthentication().getName();
        User user=userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Trajet trajet = trajetRepository.findById(id).orElseThrow(() -> new RuntimeException("trajet not found"));
        //verification coté securité
        Boolean isClient=trajet.getClient().getId().equals(user.getId());
        Boolean isChauffeur=trajet.getChauffeur()!=null && trajet.getChauffeur().getId().equals(user.getId());
        if (!isChauffeur && !isClient){
            throw new RuntimeException("Access denied");
        }
        return trajetMapper.toDTO(trajet);
    }

    public List<TrajetResponse> getAvailableTrajetsForDriver() {

        // 🔐 récupérer chauffeur connecté
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Chauffeur chauffeur = chauffeurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Chauffeur not found"));

        // 🚫 vérifier disponibilité du chauffeur
        if (chauffeur.getAvailability().name().equals("FALSE")) {
            return Collections.emptyList();
        }

        double lat = chauffeur.getCurrent_latitude();
        double lon = chauffeur.getCurrent_longitude();

        Set<Trajet> trajetsSet = new LinkedHashSet<>();

        // 🔁 expansion intelligente (1km → 9km)
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

}
