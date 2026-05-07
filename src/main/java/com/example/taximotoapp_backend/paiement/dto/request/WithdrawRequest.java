package com.example.taximotoapp_backend.paiement.dto.request;

import lombok.Data;

@Data
public class WithdrawRequest {
    private Double amount;
    private Long cardId;
}

