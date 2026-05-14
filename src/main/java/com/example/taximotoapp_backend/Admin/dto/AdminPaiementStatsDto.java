package com.example.taximotoapp_backend.Admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminPaiementStatsDto {
    private Long totalTransactions;
    private Long totalPaiements;
    private Double totalDriverRevenue;
}
