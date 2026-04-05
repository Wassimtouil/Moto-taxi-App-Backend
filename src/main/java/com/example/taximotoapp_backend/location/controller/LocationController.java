package com.example.taximotoapp_backend.location.controller;

import com.example.taximotoapp_backend.location.dto.LocationRequest;
import com.example.taximotoapp_backend.location.response.LocationResponse;
import com.example.taximotoapp_backend.location.service.LocationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    /**
     * @deprecated This REST endpoint is deprecated. Use WebSocket endpoint /app/location.update instead.
     * Location tracking is now 100% WebSocket-driven for real-time, low-latency updates.
     */
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
}
