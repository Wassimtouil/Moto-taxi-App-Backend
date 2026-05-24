package com.example.taximotoapp_backend.automation_reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopDriverDTO {

    private String driverName;

    private Double revenue;

    private Long trips;
}