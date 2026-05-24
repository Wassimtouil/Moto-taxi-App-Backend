package com.example.taximotoapp_backend.automation_reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyReportDTO {
    private Double todayRevenue;
    private Long totalTrips;
    private Double cancellationRate;
    private Long newClients;
    private Long inactiveDrivers;
    private List<TopDriverDTO> topDrivers;
    private PaymentStatsDTO paymentStats;
    private List<Map<String, Object>> reclamations;
}