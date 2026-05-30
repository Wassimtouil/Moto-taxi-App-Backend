package com.example.taximotoapp_backend.geocode.dto;

import lombok.Data;

@Data
public class GeocodingSuggestionDTO {
    private String name;
    private String address;
    private double lat;
    private double lng;
}
