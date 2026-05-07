package com.example.taximotoapp_backend.wallet.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardResponse {
    private Long id;
    private String cardHolderName;
    private String last4Digits;
    private String brand;
    private String expiryMonth;
    private String expiryYear;
    private Boolean isDefault;
    private LocalDateTime createdAt;
}
