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
}