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
public class AdminTrajetDto {
    private Long id;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    // Localisation
    private String pickupAddress;
    private String destinationAddress;
    private Double distanceKm;
    private Integer durationMinutes;

    // Prix & Paiement
    private Double price;
    private String paymentMethod;

    // Client
    private Long clientId;
    private String clientName;

    // Chauffeur
    private Long chauffeurId;
    private String chauffeurName;

    // Annulation
    private String cancelledBy;

    // Détection de suspicion / fraude
    private Boolean isSuspect;
    private String suspicionReason;
}
