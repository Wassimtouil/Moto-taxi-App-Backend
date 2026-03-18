package com.example.taximotoapp_backend.trajet.controller;

import com.example.taximotoapp_backend.trajet.dto.TrajetRequest;
import com.example.taximotoapp_backend.trajet.response.TrajetResponse;
import com.example.taximotoapp_backend.trajet.service.TrajetService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/trajets")
public class TrajetController {
    private final TrajetService trajetService;

    @PostMapping("/createTrajet")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> createTrajet(@RequestBody TrajetRequest trajetRequest) {
        try {
            TrajetResponse response = trajetService.createTrajet(trajetRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}
