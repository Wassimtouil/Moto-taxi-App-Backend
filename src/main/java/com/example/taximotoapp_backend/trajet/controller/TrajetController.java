package com.example.taximotoapp_backend.trajet.controller;

import com.example.taximotoapp_backend.trajet.dto.TrajetRequest;
import com.example.taximotoapp_backend.trajet.response.TrajetResponse;
import com.example.taximotoapp_backend.trajet.service.TrajetService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/cancel/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> cancelTrajet(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(trajetService.annulerTrajet(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('CHAUFFEUR')")
    @PostMapping("/{id}/response")
    public ResponseEntity<?> handleResponse(
            @PathVariable Long id,
            @RequestParam String action) {
        try {
            trajetService.handleDriverResponse(id, action);
            return ResponseEntity.ok(Map.of("message", "Action processed"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/start/{id}")
    @PreAuthorize("hasRole('CHAUFFEUR')")
    public ResponseEntity<?> startTrajet(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(trajetService.startTrajet(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/end/{id}")
    @PreAuthorize("hasRole('CHAUFFEUR')")
    public ResponseEntity<?> endTrajet(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(trajetService.terminerTrajet(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("errror", e.getMessage()));
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/getTrajetById/{id}")
    public ResponseEntity<?> getTrajetById(@PathVariable Long id){
        try{
            return ResponseEntity.ok(trajetService.getTrajetById(id));
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error",e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('CHAUFFEUR')")
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableTrajets() {
        return ResponseEntity.ok(trajetService.getAvailableTrajetsForDriver());
    }

}
