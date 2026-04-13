package com.example.taximotoapp_backend.reclamation.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReclamationResponse {
    private Long id;
    private String objet;
    private String contenu;
    private LocalDateTime createdAt;
}
