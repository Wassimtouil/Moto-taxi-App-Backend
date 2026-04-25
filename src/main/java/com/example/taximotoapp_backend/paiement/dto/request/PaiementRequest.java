package com.example.taximotoapp_backend.paiement.dto.request;

import com.example.taximotoapp_backend.model.enumClass.PaiementType;
import lombok.Data;

@Data
public class PaiementRequest {
    private Long trajetId;
    private Double montant;
    private PaiementType type;
}
