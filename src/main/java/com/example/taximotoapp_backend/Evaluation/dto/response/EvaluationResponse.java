package com.example.taximotoapp_backend.Evaluation.dto.response;

import com.example.taximotoapp_backend.model.enumClass.QuickChoices;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EvaluationResponse {
    private Long id;
    private double note;
    private String commentaire;
    private QuickChoices quickChoices;
    private String clientNom;
    private String chauffeurNom;
    private LocalDateTime dateEvaluation;
}
