package com.example.taximotoapp_backend.reclamation.mapper;

import com.example.taximotoapp_backend.reclamation.dto.response.ReclamationResponse;
import com.example.taximotoapp_backend.reclamation.model.Reclamation;
import org.springframework.stereotype.Component;

@Component
public class ReclamationMapper {

    public ReclamationResponse toResponse(Reclamation reclamation) {
        ReclamationResponse response = new ReclamationResponse();
        response.setId(reclamation.getId());
        response.setObjet(reclamation.getObjet());
        response.setMessage(reclamation.getMessage());
        response.setDateReclamation(reclamation.getDateReclamation());
        response.setAdminResponse(reclamation.getAdminResponse());
        response.setReclamationStatus(reclamation.getReclamationStatus());
        return response;
    }
}
