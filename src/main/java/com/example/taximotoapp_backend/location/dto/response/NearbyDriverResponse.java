package com.example.taximotoapp_backend.location.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NearbyDriverResponse {
    private Long id;
    private String fullName;
    private Double latitude;
    private Double longitude;
    private Boolean isAvailable;
    private Double rating;
    private String photoUrl;
    private String vehicleModel;
    private String vehiclePlate;
}
