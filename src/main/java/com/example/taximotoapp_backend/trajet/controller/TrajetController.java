package com.example.taximotoapp_backend.trajet.controller;

import com.example.taximotoapp_backend.trajet.dto.request.TrajetRequest;
import com.example.taximotoapp_backend.trajet.dto.request.TrajetPreviewRequest;
import com.example.taximotoapp_backend.trajet.dto.response.TrajetResponse;
import com.example.taximotoapp_backend.trajet.dto.response.TrajetPreviewResponse;
import com.example.taximotoapp_backend.trajet.service.TrajetService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;

import java.util.Collections;
import java.util.List;
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

    @PostMapping("/preview")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> previewTrajet(@RequestBody TrajetPreviewRequest request) {
        try {
            TrajetPreviewResponse response = trajetService.previewTrajet(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cancel/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'CHAUFFEUR')")
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

    @PostMapping("/{id}/arrived")
    @PreAuthorize("hasRole('CHAUFFEUR')")
    public ResponseEntity<?> driverArrived(@PathVariable Long id) {
        System.out.println("📍 Driver Arrival REST call for trajet: " + id);
        try {
            return ResponseEntity.ok(trajetService.driverArrivedAtPickup(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
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

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/active")
    public ResponseEntity<?> getActiveTrajet() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isDriver = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_CHAUFFEUR"));

            if (isDriver) {
                return trajetService.getActiveTrajetForDriver()
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.noContent().build());
            } else {
                return trajetService.getActiveTrajetForClient()
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.noContent().build());
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @MessageMapping("/trajet.arrived")
    public void driverArrivedFastPing(Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        if (payload == null || !payload.containsKey("trajetId")) return;
        Long trajetId = Long.valueOf(payload.get("trajetId").toString());
        String email = (String) headerAccessor.getSessionAttributes().get("username");
        if (email != null) {
            System.out.println("📍 Driver Arrival FAST PING for trajet: " + trajetId + " by " + email);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);
            try {
                trajetService.driverArrivedAtPickup(trajetId);
            } catch (RuntimeException e) {
                if ("User not found".equals(e.getMessage())) throw e;
                System.err.println("❌ Error processing fast ping arrived: " + e.getMessage());
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
    }

    @MessageMapping("/trajet.started")
    public void driverStartedFastPing(Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        if (payload == null || !payload.containsKey("trajetId")) return;
        Long trajetId = Long.valueOf(payload.get("trajetId").toString());
        String email = (String) headerAccessor.getSessionAttributes().get("username");
        if (email != null) {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);
            try {
                trajetService.startTrajet(trajetId);
            } catch (RuntimeException e) {
                if ("User not found".equals(e.getMessage())) throw e;
                // Ignore other errors (already started)
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
    }

    @MessageMapping("/trajet.completed")
    public void driverCompletedFastPing(Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        if (payload == null || !payload.containsKey("trajetId")) return;
        Long trajetId = Long.valueOf(payload.get("trajetId").toString());
        String email = (String) headerAccessor.getSessionAttributes().get("username");
        if (email != null) {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);
            try {
                trajetService.terminerTrajet(trajetId);
            } catch (RuntimeException e) {
                if ("User not found".equals(e.getMessage())) throw e;
                // Ignore other errors (already completed)
            } finally {
                SecurityContextHolder.clearContext();
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
