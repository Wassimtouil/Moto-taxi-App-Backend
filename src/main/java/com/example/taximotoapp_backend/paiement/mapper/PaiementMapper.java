package com.example.taximotoapp_backend.paiement.mapper;

import com.example.taximotoapp_backend.paiement.dto.request.PaiementRequest;
import com.example.taximotoapp_backend.paiement.dto.response.PaiementResponse;
import com.example.taximotoapp_backend.paiement.model.Paiement;
import org.springframework.stereotype.Component;

@Component

public class PaiementMapper {
    public PaiementResponse toResponse(Paiement p){
        PaiementResponse response=new PaiementResponse();
        response.setId(p.getId());
        response.setType(p.getType());
        response.setDatePaiement(p.getDatePaiement());
        response.setStatus(p.getStatus());
        response.setMontant(p.getMontant());
        return response;
    }
}
