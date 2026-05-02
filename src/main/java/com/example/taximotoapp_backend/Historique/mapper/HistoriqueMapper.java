package com.example.taximotoapp_backend.Historique.mapper;

import com.example.taximotoapp_backend.Historique.dto.response.HistoriqueClientResponse;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import org.springframework.stereotype.Component;

@Component
public class HistoriqueMapper {

    public HistoriqueClientResponse toHistoriqueClientResponse(Trajet trajet) {
        HistoriqueClientResponse historiqueClientResponse = new HistoriqueClientResponse();
        historiqueClientResponse.setPrix(trajet.getPrice());
        historiqueClientResponse.setDepart(trajet.getTrajetLocation().getPickupAddress());
        historiqueClientResponse.setDestination(trajet.getTrajetLocation().getDestinationAddress());
        historiqueClientResponse.setNomChauffeur(trajet.getChauffeur().getFullName());
        historiqueClientResponse.setModePaiement(trajet.getPaiement().getType().name());
        historiqueClientResponse.setStatutPaiement(trajet.getPaiement().getStatus().name());
        historiqueClientResponse.setNoteDonnee(trajet.getEvaluation().getNote());
        historiqueClientResponse.setDateCourse(trajet.getStartedAt());
        return historiqueClientResponse;
    }
}