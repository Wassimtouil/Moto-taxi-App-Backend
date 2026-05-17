package com.example.taximotoapp_backend.poi.dto.response;

import lombok.Data;

@Data
public class PoiResponse {
    private Long id;
    private String name;
    private String category;
    private String city;
    private Double latitude;
    private Double longitude;
    private String address;
}
