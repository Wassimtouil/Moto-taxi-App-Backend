package com.example.taximotoapp_backend.location.controller;

import com.example.taximotoapp_backend.location.dto.LocationRequest;
import com.example.taximotoapp_backend.location.response.LocationResponse;
import com.example.taximotoapp_backend.location.service.LocationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/location")
@AllArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class LocationController {
    private final LocationService locationService;


    @Deprecated
    @PreAuthorize("hasRole('CHAUFFEUR') or hasRole('CLIENT')")
    @PatchMapping("/updateLocation")
    public ResponseEntity<?> updateLocation(@RequestBody LocationRequest locationRequest, Principal principal){
        try {
            String email = principal.getName();
            LocationResponse response = locationService.updateLocation(locationRequest, email);
            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error",e.getMessage()));
        }
    }

    // --- WebSocket Endpoint (STOMP) ---
    // Cette méthode gère les mises à jour en temps réel envoyées via /app/location.update
    @MessageMapping("/location.update")
    public void updateLocationWebSocket(@RequestBody LocationRequest locationRequest, SimpMessageHeaderAccessor headerAccessor) {
        String email = (String) headerAccessor.getSessionAttributes().get("username");
        if (email != null) {
            locationService.updateLocation(locationRequest, email);
        }
    }
}
