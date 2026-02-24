package com.example.taximotoapp_backend.Authentification.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
    private String firebaseUid;  // login (facebook/google)
}
