package com.example.taximotoapp_backend.wallet.dto.request;

import lombok.Data;

@Data
public class DepositRequest {
    private Double amount;
    private Long cardId;
}
