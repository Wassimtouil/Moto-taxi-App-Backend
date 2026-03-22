package com.example.taximotoapp_backend.location.response;

import lombok.Data;

@Data
public class LocationResponse {
    private Double latitude;
    private Double longitude;
    private String message;
}
