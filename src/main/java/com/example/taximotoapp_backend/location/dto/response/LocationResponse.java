package com.example.taximotoapp_backend.location.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LocationResponse {
    private Double latitude;
    private Double longitude;
    private String message;
}
