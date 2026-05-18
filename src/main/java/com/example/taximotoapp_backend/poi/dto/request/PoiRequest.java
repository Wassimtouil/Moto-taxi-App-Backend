package com.example.taximotoapp_backend.poi.dto.request;

import lombok.Data;

@Data
public class PoiRequest {
    private String name;
    private String category;
    private String city;
    private Double latitude;
    private Double longitude;
    private String address;
}
