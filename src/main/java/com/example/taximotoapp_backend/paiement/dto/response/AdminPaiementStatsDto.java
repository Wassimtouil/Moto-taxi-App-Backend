package com.example.taximotoapp_backend.paiement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminPaiementStatsDto {
    private Double totalRevenue;
    private Long totalTransactions;
    private Long totalPaiements;
    private Double totalDriverRevenue;
}
