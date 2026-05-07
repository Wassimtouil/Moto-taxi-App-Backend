package com.example.taximotoapp_backend.paiement.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private Double amount;
    private String type;
    private String status;
    private String description;
    private LocalDateTime timestamp;
}

