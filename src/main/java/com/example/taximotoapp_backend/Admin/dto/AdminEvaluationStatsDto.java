package com.example.taximotoapp_backend.Admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminEvaluationStatsDto {
    private double globalAverage;
    private long totalEvaluations;
    private Map<Integer, Long> ratingDistribution; // key: 1 to 5, value: count
    private Map<String, Double> criteriaAverages; // key: "Conduite", etc., value: average
}
