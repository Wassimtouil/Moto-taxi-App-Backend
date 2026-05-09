package com.example.taximotoapp_backend.Evaluation.dto.request;

import lombok.Data;

@Data
public class EvaluationRequest {
    private Long trajetId;
    private double note;
    private String commentaire;
    private Integer noteConduite;
    private Integer noteVehicule;
    private Integer notePonctualite;
    private Integer noteService;
    private Integer noteExperience;
    private Integer noteComportement;
}
