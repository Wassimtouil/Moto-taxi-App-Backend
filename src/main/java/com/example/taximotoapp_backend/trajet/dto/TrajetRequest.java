package com.example.taximotoapp_backend.trajet.dto;

import com.example.taximotoapp_backend.model.enumClass.TripStatus;
import lombok.Data;

@Data
public class TrajetRequest {
    //deja le jwt est recuperer automatiquement avec le request pour rechercher ensuite l'id client
    private Double pickupLatitude;
    private Double pickupLongitude;
    private Double destinationLatitude;
    private Double destinationLongitude;
}
