package com.example.taximotoapp_backend.User.controller;

import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.model.enumClass.TripStatus;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import com.example.taximotoapp_backend.trajet.repository.TrajetRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/driver")
@PreAuthorize("hasRole('CHAUFFEUR')")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class DriverStatsController {
    private final TrajetRepository trajetRepository;
    private final UserRepository userRepository;

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User driver = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Chauffeur not found"));

        List<Trajet> completed = trajetRepository.findByChauffeurIdAndStatus(driver.getId(), TripStatus.Completed);

        double totalEarnings = completed.stream()
                .mapToDouble(Trajet::getPrice)
                .sum();

        long todayBookings = completed.stream()
                .filter(t -> t.getCompletedAt() != null && t.getCompletedAt().toLocalDate().equals(LocalDate.now()))
                .count();

        return ResponseEntity.ok(Map.of(
                "overallEarning", totalEarnings,
                "todayBookings", (int) todayBookings
        ));
    }
}
