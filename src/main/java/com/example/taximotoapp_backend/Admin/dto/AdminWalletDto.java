package com.example.taximotoapp_backend.Admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminWalletDto {
    private Long id;
    private Double balance;
    private Double cashBalance;
    private String currency;
    private Long userId;
    private String userName;
    private String userEmail;
    private String userRole;
    private String userPhotoBase64;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

