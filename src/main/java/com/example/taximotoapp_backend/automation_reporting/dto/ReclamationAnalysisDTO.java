package com.example.taximotoapp_backend.automation_reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReclamationAnalysisDTO {

    private String summary;

    private String riskLevel;

    private String recommendation;
}