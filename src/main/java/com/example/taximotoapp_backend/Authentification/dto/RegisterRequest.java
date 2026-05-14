package com.example.taximotoapp_backend.Authentification.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private String role; // CLIENT ou CHAUFFEUR
    private String firebaseUid; // facultatif
    private String gender;
    private String drivingLicenceBase64; // facultatif - pour chauffeurs
    private String carteGriseBase64;     // facultatif - pour chauffeurs (carte grise moto)
    private String photoBase64;          // facultatif - photo de profil
    private String vehicleModel;         // facultatif - pour chauffeurs
    private String vehiclePlate;         // facultatif - pour chauffeurs
}
