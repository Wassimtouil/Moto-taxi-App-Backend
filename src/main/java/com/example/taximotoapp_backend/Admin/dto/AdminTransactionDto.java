package com.example.taximotoapp_backend.Admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminTransactionDto {
    private Long id;
    private Double amount;
    private String type;
    private String status;
    private String description;
    private LocalDateTime timestamp;
    private Long walletId;
    private Long userId;
    private String userName;
    private String userRole;
    private String userPhotoBase64;
    private String source;
}
