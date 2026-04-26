package com.example.taximotoapp_backend.Evaluation.dto.request;

import com.example.taximotoapp_backend.model.enumClass.QuickChoices;
import lombok.Data;
import java.util.List;

@Data
public class EvaluationRequest {
    private Long trajetId;
    private double note;
    private String commentaire;
    private QuickChoices quickChoices;
}
