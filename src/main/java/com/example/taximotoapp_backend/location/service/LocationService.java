package com.example.taximotoapp_backend.location.service;

import com.example.taximotoapp_backend.User.model.Chauffeur;
import com.example.taximotoapp_backend.User.repository.ChauffeurRepository;
import com.example.taximotoapp_backend.location.dto.LocationRequest;
import com.example.taximotoapp_backend.location.response.LocationResponse;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import com.example.taximotoapp_backend.trajet.repository.TrajetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final ChauffeurRepository chauffeurRepository;
    private final TrajetRepository trajetRepository;
    private final SimpMessagingTemplate messagingTemplate;
    public LocationResponse updateLocation(LocationRequest locationRequest){
        // récupérer chauffeur connecté
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Chauffeur chauffeur = chauffeurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Chauffeur not found"));

        // mettre à jour la position dans la DB
        chauffeur.setCurrent_latitude(locationRequest.getLatitude());
        chauffeur.setCurrent_longitude(locationRequest.getLongitude());
        chauffeurRepository.save(chauffeur);

        // récupérer le trajet actif du chauffeur (si nécessaire)
        Optional<Trajet> trajetOpt = trajetRepository.findActiveTrajetByChauffeurId(chauffeur.getId());
        trajetOpt.ifPresent(trajet -> {
            // envoyer la position au client via WebSocket
            Map<String, Object> payload = new HashMap<>();
            payload.put("driverLatitude", locationRequest.getLatitude());
            payload.put("driverLongitude", locationRequest.getLongitude());
            messagingTemplate.convertAndSend(
                    "/topic/driverLocation/" + trajet.get().getClient().getId(),
                    payload
            );
        });

        return new LocationResponse(locationRequest.getLatitude(),
                locationRequest.getLongitude(),
                "Location updated successfully");
    }

}
