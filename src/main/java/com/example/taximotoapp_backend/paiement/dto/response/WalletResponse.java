package com.example.taximotoapp_backend.paiement.dto.response;

import com.example.taximotoapp_backend.Historique.dto.response.TransactionResponse;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {
    private Long id;
    private Double balance;
    private Double cashBalance;
    private String currency;
    private List<TransactionResponse> recentTransactions;
}

