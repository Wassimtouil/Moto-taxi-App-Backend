package com.example.taximotoapp_backend.Evaluation.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EvaluationResponse {
    private Long id;
    private int note;
    private String commentaire;
    private String clientNom;
    private String chauffeurNom;
    private LocalDateTime dateEvaluation;
}
