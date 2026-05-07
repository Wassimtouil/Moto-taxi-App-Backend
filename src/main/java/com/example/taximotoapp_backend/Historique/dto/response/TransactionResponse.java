package com.example.taximotoapp_backend.Historique.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    private Long id;
    private Double amount;
    private String type;
    private String status;
    private String description;
    private LocalDateTime timestamp;
}
