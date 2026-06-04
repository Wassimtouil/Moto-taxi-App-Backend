package com.example.taximotoapp_backend.User.dto;

import com.example.taximotoapp_backend.User.model.Chauffeur;
import com.example.taximotoapp_backend.User.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private String fullName;
    private String role;
    private String gender;
    private Boolean isVerified;
    private String activityStatus;
    private String phoneNumber;
    private LocalDateTime createdAt;

    // Champs spécifiques Chauffeur (optionnels)
    private String vehicleModel;
    private String vehiclePlate;


    public UserDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.role = user.getRole() != null ? user.getRole().name() : null;
        this.gender = user.getGender() != null ? user.getGender().name() : null;
        this.isVerified = user.getIsVerified();
        this.activityStatus = user.getActivityStatus() != null ? user.getActivityStatus().name() : null;
        this.phoneNumber = user.getPhoneNumber();
        this.createdAt=user.getCreatedAt();

        if (user instanceof Chauffeur) {
            Chauffeur c = (Chauffeur) user;
            this.vehicleModel = c.getVehicleModel();
            this.vehiclePlate = c.getVehiclePlate();

        }
    }
}

