package com.example.taximotoapp_backend.reclamation.mapper;

import com.example.taximotoapp_backend.model.enumClass.ReclamationsType;
import com.example.taximotoapp_backend.reclamation.dto.response.ReclamationResponse;
import com.example.taximotoapp_backend.reclamation.dto.response.ReclamationResponseAdmin;
import com.example.taximotoapp_backend.reclamation.model.Reclamation;
import org.springframework.stereotype.Component;

@Component
public class ReclamationMapper {

    public ReclamationResponse toResponse(Reclamation reclamation) {
        ReclamationResponse response = new ReclamationResponse();
        response.setId(reclamation.getId());

        // ✅ enum → String
        response.setObjet(
                reclamation.getObjet() != null ? reclamation.getObjet().name() : null
        );


        response.setMessage(reclamation.getMessage());
        response.setDateReclamation(reclamation.getDateReclamation());
        response.setAdminResponse(reclamation.getAdminResponse());
        response.setReclamationStatus(reclamation.getReclamationStatus());

        return response;
    }

    public ReclamationResponseAdmin toAdminResponse(Reclamation reclamation) {
        ReclamationResponseAdmin response = new ReclamationResponseAdmin();
        response.setId(reclamation.getId());

        // ✅ enum → String
        response.setObjet(
                reclamation.getObjet() != null ? reclamation.getObjet().name() : null
        );


        response.setMessage(reclamation.getMessage());
        response.setDateReclamation(reclamation.getDateReclamation());
        response.setAdminResponse(reclamation.getAdminResponse());
        response.setReclamationStatus(
                reclamation.getReclamationStatus() != null ? reclamation.getReclamationStatus().name() : null
        );

        if (reclamation.getUser() != null) {
            response.setUserName(reclamation.getUser().getFullName());
            response.setUserEmail(reclamation.getUser().getEmail());
            response.setUserId(reclamation.getUser().getId());
        }

        return response;
    }
}