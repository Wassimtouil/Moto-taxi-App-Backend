package com.example.taximotoapp_backend.User.controller;

import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.User.service.DriverStatsService;
import com.example.taximotoapp_backend.model.enumClass.TripStatus;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import com.example.taximotoapp_backend.trajet.repository.TrajetRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taximotoapp_backend.User.model.Chauffeur;
import com.example.taximotoapp_backend.User.repository.ChauffeurRepository;
import com.example.taximotoapp_backend.model.enumClass.Availability;
import com.example.taximotoapp_backend.model.enumClass.ActivityStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/driver")
@PreAuthorize("hasRole('CHAUFFEUR')")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class DriverStatsController {
    private final DriverStatsService driverStatsService;

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(driverStatsService.getStats());
    }

    @GetMapping("/availability")
    public ResponseEntity<?> getAvailability() {
        return ResponseEntity.ok(driverStatsService.getAvailability());
    }

    @PatchMapping("/availability")
    public ResponseEntity<?> updateAvailability(@RequestBody Map<String, Boolean> payload) {
       return ResponseEntity.ok(driverStatsService.updateAvailability(payload));
    }
}
