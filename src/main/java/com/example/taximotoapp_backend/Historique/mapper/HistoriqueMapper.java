package com.example.taximotoapp_backend.Historique.mapper;

import com.example.taximotoapp_backend.Historique.dto.response.HistoriqueChauffeurResponse;
import com.example.taximotoapp_backend.Historique.dto.response.HistoriqueClientResponse;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import org.springframework.stereotype.Component;

@Component
public class HistoriqueMapper {

    public HistoriqueClientResponse toHistoriqueClientResponse(Trajet trajet) {
        HistoriqueClientResponse response = new HistoriqueClientResponse();
        response.setPrix(trajet.getPrice());
        if (trajet.getTrajetLocation() != null) {
            response.setDepart(trajet.getTrajetLocation().getPickupAddress());
            response.setDestination(trajet.getTrajetLocation().getDestinationAddress());
            response.setEncodedPolyline(trajet.getTrajetLocation().getEncodedPolyline());
        }
        if (trajet.getChauffeur() != null) {
            response.setNomChauffeur(trajet.getChauffeur().getFullName());
        }
        if (trajet.getPaiement() != null) {
            response.setModePaiement(trajet.getPaiement().getType() != null ? trajet.getPaiement().getType().name() : "N/A");
            response.setStatutPaiement(trajet.getPaiement().getStatus() != null ? trajet.getPaiement().getStatus().name() : "N/A");
        }
        if (trajet.getEvaluation() != null) {
            response.setNoteDonnee(trajet.getEvaluation().getNote());
        }
        response.setDateCourse(trajet.getStartedAt() != null ? trajet.getStartedAt() : trajet.getRequestedAt());
        response.setStatutTrajet(trajet.getStatus() != null ? trajet.getStatus().name() : "UNKNOWN");
        return response;
    }

    public HistoriqueChauffeurResponse toHistoriqueChauffeurResponse(Trajet trajet) {
        HistoriqueChauffeurResponse response = new HistoriqueChauffeurResponse();
        response.setPrix(trajet.getPrice());
        if (trajet.getTrajetLocation() != null) {
            response.setDepart(trajet.getTrajetLocation().getPickupAddress());
            response.setDestination(trajet.getTrajetLocation().getDestinationAddress());
            response.setEncodedPolyline(trajet.getTrajetLocation().getEncodedPolyline());
        }
        if (trajet.getPaiement() != null) {
            response.setModePaiement(trajet.getPaiement().getType() != null ? trajet.getPaiement().getType().name() : "N/A");
            response.setStatutPaiement(trajet.getPaiement().getStatus() != null ? trajet.getPaiement().getStatus().name() : "N/A");
        }
        if (trajet.getEvaluation() != null) {
            response.setNoteDonnee(trajet.getEvaluation().getNote());
            response.setCommentaire(trajet.getEvaluation().getCommentaire());
        }
        response.setDateCourse(trajet.getStartedAt() != null ? trajet.getStartedAt() : trajet.getRequestedAt());
        response.setStatutTrajet(trajet.getStatus() != null ? trajet.getStatus().name() : "UNKNOWN");
        return response;
    }


}