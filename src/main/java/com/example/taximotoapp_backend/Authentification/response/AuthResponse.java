package com.example.taximotoapp_backend.Authentification.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private String gender;
    private String photoUrl;

    @JsonProperty("isVerified")
    private Boolean isVerified;
}

