package com.example.taximotoapp_backend.nlpVocale.dto;

import lombok.Data;

@Data
public class NlpResponseDTO {

    private String text;
    private String place;
    private GpsDTO gps;
}