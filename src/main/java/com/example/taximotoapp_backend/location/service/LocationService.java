package com.example.taximotoapp_backend.location.service;

import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.User.repository.ChauffeurRepository;
import com.example.taximotoapp_backend.location.dto.request.LocationRequest;
import com.example.taximotoapp_backend.location.model.Location;
import com.example.taximotoapp_backend.location.repository.LocationRepository;
import com.example.taximotoapp_backend.location.dto.response.LocationResponse;
import com.example.taximotoapp_backend.location.dto.response.NearbyDriverResponse;
import com.example.taximotoapp_backend.model.enumClass.Role;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import com.example.taximotoapp_backend.trajet.repository.TrajetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final UserRepository userRepository;
    private final TrajetRepository trajetRepository;
    private final LocationRepository locationRepository;
    private final ChauffeurRepository chauffeurRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @org.springframework.transaction.annotation.Transactional
    public LocationResponse updateLocation(LocationRequest locationRequest, String email){
        System.out.println("📍 [BACKEND] Updating location for user: " + email + " (" + locationRequest.getLatitude() + ", " + locationRequest.getLongitude() + ")");
        // récupérer utilisateur par email (CLIENT ou CHAUFFEUR)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // mettre à jour la position dans la table Location (séparée du User)
        Location location = user.getLocation();
        if (location == null) {
            location = new Location();
            location.setUser(user);
            user.setLocation(location);
        }
        location.setLatitude(locationRequest.getLatitude());
        location.setLongitude(locationRequest.getLongitude());
        locationRepository.save(location);

        // déterminer le rôle et récupérer les trajets actifs
        if (user.getRole() == Role.ROLE_CHAUFFEUR) {
            // récupérer les trajets actifs du chauffeur (liste ordonnée par ID DESC)
            List<Trajet> trajets = trajetRepository.findActiveTrajetByChauffeurId(user.getId());
            if (!trajets.isEmpty()) {
                // prendre le trajet le plus récent (premier élément)
                Trajet trajet = trajets.get(0);
                // envoyer la position au client via WebSocket
                Map<String, Object> payload = new HashMap<>();
                payload.put("latitude", locationRequest.getLatitude());
                payload.put("longitude", locationRequest.getLongitude());
                messagingTemplate.convertAndSend(
                        "/topic/driverLocation/" + trajet.getClient().getId(),
                        payload
                );
            }
        } else if (user.getRole() == Role.ROLE_CLIENT) {
            // récupérer les trajets actifs du client (liste ordonnée par ID DESC)
            List<Trajet> trajets = trajetRepository.findActiveTrajetByClientId(user.getId());
            if (!trajets.isEmpty()) {
                // prendre le trajet le plus récent (premier élément)
                Trajet trajet = trajets.get(0);
                // envoyer la position au chauffeur via WebSocket
                Map<String, Object> payload = new HashMap<>();
                payload.put("latitude", locationRequest.getLatitude());
                payload.put("longitude", locationRequest.getLongitude());
                messagingTemplate.convertAndSend(
                        "/topic/clientLocation/" + trajet.getChauffeur().getId(),
                        payload
                );
            }
        }

        return new LocationResponse(locationRequest.getLatitude(),
                locationRequest.getLongitude(),
                "Location updated successfully");
    }

    public List<NearbyDriverResponse> getNearbyDrivers(double lat, double lon, double radius) {
        List<com.example.taximotoapp_backend.User.model.Chauffeur> drivers = chauffeurRepository
                .findAllNearbyDrivers(lat, lon, radius);

        return drivers.stream()
                .filter(c -> c.getLocation() != null)
                .map(c -> NearbyDriverResponse.builder()
                        .id(c.getId())
                        .fullName(c.getFullName())
                        .latitude(c.getLocation().getLatitude())
                        .longitude(c.getLocation().getLongitude())
                        .isAvailable(c.getAvailability() == com.example.taximotoapp_backend.model.enumClass.Availability.TRUE)
                        .rating(c.getNoteMoyenne() != null ? c.getNoteMoyenne() : 0.0)
                        .photoUrl(c.getPhotoUrl())
                        .vehicleModel(c.getVehicleModel())
                        .vehiclePlate(c.getVehiclePlate())
                        .build())
                .toList();
    }
}
