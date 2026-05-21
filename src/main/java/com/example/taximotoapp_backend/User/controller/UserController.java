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
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

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

    @GetMapping("/profile")
    @PreAuthorize("hasRole('CLIENT') or hasRole('CHAUFFEUR')")
    public ResponseEntity<?> getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("id", user.getId());
        resp.put("fullName", user.getFullName());
        resp.put("email", user.getEmail());
        resp.put("role", user.getRole() != null ? user.getRole().name() : null);
        resp.put("gender", user.getGender() != null ? user.getGender().name() : null);
        resp.put("isVerified", user.getIsVerified());
        resp.put("photoUrl", (user instanceof com.example.taximotoapp_backend.User.model.Chauffeur)
                ? ((com.example.taximotoapp_backend.User.model.Chauffeur) user).getPhotoUrl()
                : user.getPhotoBase64());

        return ResponseEntity.ok(resp);
    }

    @PatchMapping("/profile")
    @PreAuthorize("hasRole('CLIENT') or hasRole('CHAUFFEUR')")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> payload) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String photoBase64 = payload.get("photoBase64");
        if (photoBase64 != null) {
            user.setPhotoBase64(photoBase64);
            if (user instanceof com.example.taximotoapp_backend.User.model.Chauffeur) {
                ((com.example.taximotoapp_backend.User.model.Chauffeur) user).setPhotoUrl(null);
            }
        }

        userRepository.save(user);

        Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("id", user.getId());
        resp.put("fullName", user.getFullName());
        resp.put("email", user.getEmail());
        resp.put("role", user.getRole() != null ? user.getRole().name() : null);
        resp.put("gender", user.getGender() != null ? user.getGender().name() : null);
        resp.put("isVerified", user.getIsVerified());
        resp.put("photoUrl", (user instanceof com.example.taximotoapp_backend.User.model.Chauffeur)
                ? ((com.example.taximotoapp_backend.User.model.Chauffeur) user).getPhotoUrl()
                : user.getPhotoBase64());

        return ResponseEntity.ok(resp);
    }

    @PatchMapping("/profile/password")
    @PreAuthorize("hasRole('CLIENT') or hasRole('CHAUFFEUR')")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> payload) {
        String oldPassword = payload.get("oldPassword");
        String newPassword = payload.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "oldPassword and newPassword are required"));
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Current password is incorrect"));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
