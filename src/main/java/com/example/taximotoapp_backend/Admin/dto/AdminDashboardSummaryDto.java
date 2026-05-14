package com.example.taximotoapp_backend.Admin.dto;

import com.example.taximotoapp_backend.trajet.dto.response.TrajetResponse;
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
public class AdminDashboardSummaryDto {
    private long totalUsers;
    private long totalChauffeurs;
    private long totalClients;
    private long totalTrips;
    private long tripsToday;
    private Double totalRevenue;
    private Double revenueToday;
    private List<TrajetResponse> recentTrajets;
    private List<Map<String, Object>> tripsActivity; // Daily trip counts for chart
    private List<Map<String, Object>> registrationActivity; // Daily registration counts for chart
    private List<Map<String, Object>> tripsByMonth;
    private List<Map<String, Object>> peakHours;
    private List<Map<String, Object>> peakDays;
    private List<Map<String, Object>> topZones;
    private List<Map<String, Object>> ageGroupStats;

}
