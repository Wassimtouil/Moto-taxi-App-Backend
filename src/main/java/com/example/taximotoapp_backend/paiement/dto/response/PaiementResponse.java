package com.example.taximotoapp_backend.paiement.dto.response;

import com.example.taximotoapp_backend.model.enumClass.PaiementStatus;
import com.example.taximotoapp_backend.model.enumClass.PaiementType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaiementResponse {
    private Long id;
    private Double montant;
    private PaiementType type;
    private PaiementStatus status;
    private LocalDateTime datePaiement;
}
