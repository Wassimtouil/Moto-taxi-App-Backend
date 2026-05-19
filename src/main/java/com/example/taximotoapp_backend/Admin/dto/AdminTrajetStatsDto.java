package com.example.taximotoapp_backend.Admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminTrajetStatsDto {
    private long totalTrajets;
    private long trajetsToday;
    private long completed;
    private long canceled;
    private long inProgress;   // Accepted + Arrived + Started
    private long pending;      // Created + Scheduled
    private double cancelRate; // percentage
    private double totalRevenue;
    private double revenueToday;
    private double avgPrice;
    private double avgDistanceKm;
}
