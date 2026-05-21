package com.example.taximotoapp_backend.nlpVocale.dto;

import lombok.Data;

@Data
public class GpsDTO {

    private String place;
    private Double lat;
    private Double lng;
    private String full_name;
}