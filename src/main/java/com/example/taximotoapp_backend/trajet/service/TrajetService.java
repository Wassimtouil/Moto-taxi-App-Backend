package com.example.taximotoapp_backend.trajet.service;

import com.example.taximotoapp_backend.User.model.Client;
import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.model.enumClass.TripStatus;
import com.example.taximotoapp_backend.trajet.dto.TrajetRequest;
import com.example.taximotoapp_backend.trajet.mapper.TrajetMapper;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import com.example.taximotoapp_backend.trajet.repository.TrajetRepository;
import com.example.taximotoapp_backend.trajet.response.TrajetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TrajetService {
    private final TrajetRepository trajetRepository;
    private final UserRepository userRepository;
    private final TrajetMapper trajetMapper;

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
}
