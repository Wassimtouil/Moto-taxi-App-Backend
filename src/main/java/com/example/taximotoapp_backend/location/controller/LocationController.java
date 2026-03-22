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

import java.util.Map;

@RestController
@RequestMapping("/api/location")
@AllArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class LocationController {
    private final LocationService locationService;

    @PreAuthorize("hasRole('CHAUFFEUR')")
    @PatchMapping("/updateLocation")
    public ResponseEntity<?> updateLocation(@RequestBody LocationRequest locationRequest){
        try {
            LocationResponse response = locationService.updateLocation(locationRequest);
            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error",e.getMessage()));
        }
    }
}
