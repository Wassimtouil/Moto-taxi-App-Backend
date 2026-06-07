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
    private final TrajetRepository trajetRepository;
    private final UserRepository userRepository;
    private final ChauffeurRepository chauffeurRepository;

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Chauffeur driver = chauffeurRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Chauffeur not found"));

        List<Trajet> allTrajets = trajetRepository.findByChauffeurIdOrderByRequestedAtDesc(driver.getId());
        List<Trajet> completed = allTrajets.stream().filter(t -> t.getStatus() == TripStatus.Completed).toList();
        List<Trajet> canceled = allTrajets.stream().filter(t -> t.getStatus() == TripStatus.Canceled).toList();

        double totalEarnings = completed.stream()
                .mapToDouble(Trajet::getPrice)
                .sum();

        long todayBookings = completed.stream()
                .filter(t -> t.getCompletedAt() != null && t.getCompletedAt().toLocalDate().equals(LocalDate.now()))
                .count();

        double todayEarning = completed.stream()
                .filter(t -> t.getCompletedAt() != null && t.getCompletedAt().toLocalDate().equals(LocalDate.now()))
                .mapToDouble(Trajet::getPrice)
                .sum();

        long totalWorkTimeMinutes = completed.stream()
                .filter(t -> t.getRequestedAt() != null && t.getCompletedAt() != null)
                .mapToLong(t -> java.time.Duration.between(t.getRequestedAt(), t.getCompletedAt()).toMinutes())
                .sum();

        Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("overallEarning", totalEarnings);
        resp.put("todayBookings", (int) todayBookings);
        resp.put("todayEarning", todayEarning);
        resp.put("completedTrips", completed.size());
        resp.put("totalTrips", allTrajets.size());
        resp.put("canceledTrips", canceled.size());
        resp.put("rating", driver.getNoteMoyenne() != null ? driver.getNoteMoyenne() : 5.0);
        resp.put("photoUrl", driver.getPhotoUrl() != null ? driver.getPhotoUrl() : (driver.getPhotoBase64() != null ? driver.getPhotoBase64() : ""));
        resp.put("totalWorkTimeMinutes", (int) totalWorkTimeMinutes);

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/availability")
    public ResponseEntity<?> getAvailability() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Chauffeur driver = chauffeurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Chauffeur not found"));

        return ResponseEntity.ok(Map.of("available", driver.getAvailability() == Availability.TRUE));
    }

    @PatchMapping("/availability")
    public ResponseEntity<?> updateAvailability(@RequestBody Map<String, Boolean> payload) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Chauffeur driver = chauffeurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Chauffeur not found"));

        Boolean isAvailable = payload.get("available");
        if (isAvailable != null) {
            driver.setAvailability(isAvailable ? Availability.TRUE : Availability.FALSE);
            driver.setActivityStatus(isAvailable ? ActivityStatus.ONLINE : ActivityStatus.OFFLINE);
            chauffeurRepository.save(driver);
        }

        return ResponseEntity.ok(Map.of("available", driver.getAvailability() == Availability.TRUE));
    }
}
