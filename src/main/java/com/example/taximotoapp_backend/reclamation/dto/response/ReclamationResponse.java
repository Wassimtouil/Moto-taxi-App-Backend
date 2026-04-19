package com.example.taximotoapp_backend.reclamation.dto.response;

import com.example.taximotoapp_backend.model.enumClass.ReclamationStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReclamationResponse {
    private Long id;
    private String objet;
    private String message;
    private LocalDate dateReclamation;
    private String adminResponse;
    private ReclamationStatus reclamationStatus;
}
