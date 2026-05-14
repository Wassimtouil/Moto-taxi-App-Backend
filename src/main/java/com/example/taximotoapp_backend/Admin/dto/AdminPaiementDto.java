package com.example.taximotoapp_backend.Admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminPaiementDto {
    private Long id;
    private Double montant;
    private String type;
    private String status;
    private LocalDateTime datePaiement;
    private Long trajetId;
    private Long clientId;
    private String clientName;
    private Long chauffeurId;
    private String chauffeurName;
}
