package com.example.taximotoapp_backend.Evaluation.dto.request;

import lombok.Data;

@Data
public class EvaluationRequest {
    private Long trajetId;
    private int note;
    private String commentaire;
}
