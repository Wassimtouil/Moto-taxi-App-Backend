package com.example.taximotoapp_backend.trajet.dto.response;
import com.example.taximotoapp_backend.model.enumClass.TripStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrajetResponse {
    private Long id;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private Double destinationLatitude;
    private Double destinationLongitude;
    private String pickupAddress;
    private String destinationAddress;
    private String encodedPolyline;
    private Double distanceKm;
    private Double price;
    private TripStatus status;
    private String paymentMethod;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime requestedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime scheduledAt;
    private Long clientId;
    private Long chauffeurId;
    private String clientName;
    private Double clientLatitude;
    private Double clientLongitude;
}
