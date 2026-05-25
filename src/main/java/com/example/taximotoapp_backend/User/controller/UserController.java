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
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;

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
        return ResponseEntity.ok(userService.changePassword(payload));
    }
}
