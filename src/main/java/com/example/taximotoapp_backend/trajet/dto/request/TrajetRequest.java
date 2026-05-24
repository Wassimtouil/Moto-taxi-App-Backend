package com.example.taximotoapp_backend.trajet.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class TrajetRequest {
    //deja le jwt est recuperer automatiquement avec le request pour rechercher ensuite l'id client
    private Double pickupLatitude;
    private Double pickupLongitude;
    private Double destinationLatitude;
    private Double destinationLongitude;

    private String pickupAddress;
    private String destinationAddress;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private java.time.LocalDateTime scheduledAt;

    private String preferredDriverGender;
    private Long preferredDriverId;
    private com.example.taximotoapp_backend.model.enumClass.PaiementType paymentMethod;

    // Preview-computed values — if provided, skip the Mapbox route details call
    private Double distanceKm;
    private Integer durationMinutes;
    private String encodedPolyline;
    private Double price;
}