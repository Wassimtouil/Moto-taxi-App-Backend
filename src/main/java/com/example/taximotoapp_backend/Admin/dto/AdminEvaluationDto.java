package com.example.taximotoapp_backend.Admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminEvaluationDto {
    private Long id;
    private double note;
    private String commentaire;
    private LocalDateTime dateEvaluation;

    // Client info
    private Long clientId;
    private String clientName;
    private String clientEmail;

    // Chauffeur info
    private Long chauffeurId;
    private String chauffeurName;
    private String chauffeurEmail;

    // Trajet info
    private Long trajetId;
    private String pickupAddress;
    private String destinationAddress;

    // Sub-criteria ratings
    private Integer noteConduite;
    private Integer noteVehicule;
    private Integer notePonctualite;
    private Integer noteService;
    private Integer noteExperience;
    private Integer noteComportement;
}
