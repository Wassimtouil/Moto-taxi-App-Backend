package com.example.taximotoapp_backend.Evaluation.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EvaluationResponse {
    private Long id;
    private double note;
    private String commentaire;
    private List<String> quickChoices;
    private String clientNom;
    private String chauffeurNom;
    private LocalDateTime dateEvaluation;
}
