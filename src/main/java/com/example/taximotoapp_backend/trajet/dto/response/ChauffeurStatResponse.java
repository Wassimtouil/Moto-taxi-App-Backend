package com.example.taximotoapp_backend.trajet.dto.response;

import com.example.taximotoapp_backend.model.enumClass.Availability;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChauffeurStatResponse {
    private Long id;
    private String fullName;
    private String email;
    private String vehicleModel;
    private String vehiclePlate;
    private String photoUrl;

    // Statistiques
    private long totalTrips;
    private long completedTrips;
    private long canceledTrips;
    private double totalRevenue;
    private long totalWorkTimeMinutes;
    private Double rating;
    private Availability availability;
    private boolean isVerified;
}
