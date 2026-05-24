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

    // Existants
    private Double todayRevenue;
    private Long totalTrips;
    private Double cancellationRate;
    private Long newClients;
    private Long inactiveDrivers;
    private List<TopDriverDTO> topDrivers;
    private PaymentStatsDTO paymentStats;
    private List<Map<String, Object>> reclamations;

    // Manquants — KPIs
    private Long completedTrips;
    private Long canceledTrips;
    private Long activeDrivers;
    private Long activeClients;

    // Manquants — Analytique courses
    private Double avgTripPrice;
    private Double avgDistance;
    private Double totalDistance;
    private Double avgDuration;

    // Manquants — Revenus par type
    private Double cashRevenue;
    private Double onlineRevenue;

    // Manquants — Heures de pointe & zones
    private List<Map<String, Object>> peakHours;
    private List<Map<String, Object>> topZones;

    // Manquants — Système
    private Long blockedUsers;
    private Long pendingDrivers;
    private Long reclamationCount;

    // Manquants — Comparaison hier
    private Double yesterdayRevenue;
    private Long yesterdayTrips;

    // Manquant — Résumé IA
    private String aiSummary;
}