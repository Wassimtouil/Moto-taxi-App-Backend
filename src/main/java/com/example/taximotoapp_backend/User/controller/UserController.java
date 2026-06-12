package com.example.taximotoapp_backend.User.controller;

import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.User.service.UserService;
import com.example.taximotoapp_backend.model.enumClass.ActivityStatus;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    @PatchMapping("/status")
    @PreAuthorize("hasRole('CLIENT') or hasRole('CHAUFFEUR')")
    public ResponseEntity<?> updateActivityStatus(@RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(userService.updateActivityStatus(payload));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('CLIENT') or hasRole('CHAUFFEUR')")
    public ResponseEntity<?> getProfile() {
        return ResponseEntity.ok(userService.getProfile());
    }

    @PatchMapping("/profile")
    @PreAuthorize("hasRole('CLIENT') or hasRole('CHAUFFEUR')")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(userService.updateProfile(payload));
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
