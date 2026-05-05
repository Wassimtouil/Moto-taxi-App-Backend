package com.example.taximotoapp_backend.wallet.dto.request;

import lombok.Data;

@Data
public class AddCardRequest {
    private String cardNumber;
    private String cardHolderName;
    private String expiryMonth;
    private String expiryYear;
    private String cvv;
    private String brand;
    private Boolean isDefault;
}
