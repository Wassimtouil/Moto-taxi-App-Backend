package com.example.taximotoapp_backend.reclamation.dto.response;

import com.example.taximotoapp_backend.model.enumClass.ReclamationStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ReclamationResponseAdmin {
    private Long id;
    private String objet;
    private String message;
    private LocalDate dateReclamation;
    private String adminResponse;
    private ReclamationStatus reclamationStatus;
    // Informations spécifiques pour l'admin
    private String userName;
    private String userEmail;
    private Long userId;
}