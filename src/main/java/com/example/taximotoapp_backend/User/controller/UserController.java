package com.example.taximotoapp_backend.User.controller;
import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.model.enumClass.ActivityStatus;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserRepository userRepository;
    @PatchMapping("/status")
    @PreAuthorize("hasRole('CLIENT') or hasRole('CHAUFFEUR')")
    public ResponseEntity<?> updateActivityStatus(@RequestBody Map<String, String> payload) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String statusStr = payload.get("status");
        if (statusStr != null) {
            try {
                ActivityStatus status = ActivityStatus.valueOf(statusStr.toUpperCase());
                user.setActivityStatus(status);
                userRepository.save(user);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid status. Must be ONLINE or OFFLINE"));
            }
        }
        return ResponseEntity.ok(Map.of("status", user.getActivityStatus().name()));
    }
}
