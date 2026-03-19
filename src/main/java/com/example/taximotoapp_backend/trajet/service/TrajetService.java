package com.example.taximotoapp_backend.trajet.service;

import com.example.taximotoapp_backend.User.model.Chauffeur;
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
import java.util.List;

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

    public TrajetResponse acceptTrajet(Long trajetId){
        String email= SecurityContextHolder.getContext().getAuthentication().getName();
        User Chauffeur= userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("Chauffeur not found"));
        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new RuntimeException("trajet not found"));
        if (trajet.getStatus() != TripStatus.Created){
            new RuntimeException("trajet deja pris");
        }
        trajet.setChauffeur((Chauffeur) Chauffeur);
        trajet.setStatus(TripStatus.Accepted);
        return trajetMapper.toDTO(trajetRepository.save(trajet));
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
        Trajet trajet = trajetRepository.findById(id).orElseThrow(() -> new RuntimeException("trajet not found"));
        return trajetMapper.toDTO(trajet);
    }

    public List<TrajetResponse> getAvailableTrajets() {
        return trajetRepository.findByStatus(TripStatus.Created)
                .stream()
                .map(trajetMapper::toDTO)
                .toList();
    }
}
