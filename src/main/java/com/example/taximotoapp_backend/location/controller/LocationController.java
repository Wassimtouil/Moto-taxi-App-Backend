package com.example.taximotoapp_backend.location.controller;

import com.example.taximotoapp_backend.location.dto.request.LocationRequest;
import com.example.taximotoapp_backend.location.dto.response.LocationResponse;
import com.example.taximotoapp_backend.location.service.LocationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
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

    @GetMapping("/nearby-drivers")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> getNearbyDrivers(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "5.0") double radius) {
        try {
            return ResponseEntity.ok(locationService.getNearbyDrivers(lat, lon, radius));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // --- WebSocket Endpoint (STOMP) ---
    // Cette méthode gère les mises à jour en temps réel envoyées via /app/location.update
    @MessageMapping("/location.update")
    public void updateLocationWebSocket(@RequestBody LocationRequest locationRequest, SimpMessageHeaderAccessor headerAccessor) {
        String email = (String) headerAccessor.getSessionAttributes().get("username");
        if (email != null) {
            try {
                locationService.updateLocation(locationRequest, email);
            } catch (RuntimeException e) {
                if ("User not found".equals(e.getMessage())) {
                    // Si l'utilisateur n'existe plus, on pourrait envoyer un message d'erreur spécifique
                    // ou laisser le client gérer l'absence de réponse/déconnexion.
                    // Ici on lance l'exception pour qu'elle soit gérée par handleException
                    throw e;
                }
            }
        }
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public Map<String, String> handleException(RuntimeException e) {
        if ("User not found".equals(e.getMessage())) {
            return Map.of("error", "User not found", "action", "logout");
        }
        return Map.of("error", e.getMessage());
    }
}
