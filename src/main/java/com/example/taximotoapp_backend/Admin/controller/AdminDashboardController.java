package com.example.taximotoapp_backend.Admin.controller;

import com.example.taximotoapp_backend.Admin.dto.AdminDashboardSummaryDto;
import com.example.taximotoapp_backend.Admin.dto.UserLocationDto;
import com.example.taximotoapp_backend.Admin.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<AdminDashboardSummaryDto> getDashboardSummary() {
        return ResponseEntity.ok(dashboardService.getDashboardSummary());
    }

    @GetMapping("/live-locations")
    public ResponseEntity<List<UserLocationDto>> getUserLocations() {
        return ResponseEntity.ok(dashboardService.getUserLocations());
    }
}
