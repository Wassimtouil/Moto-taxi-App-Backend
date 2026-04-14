package com.example.taximotoapp_backend.reclamation.controller;

import com.example.taximotoapp_backend.reclamation.dto.request.ReclamationRequest;
import com.example.taximotoapp_backend.reclamation.dto.response.ReclamationResponse;
import com.example.taximotoapp_backend.reclamation.service.ReclamationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reclamation")
public class ReclamationController {
    private final ReclamationService service;
    @PreAuthorize("hasAnyRole('CLIENT','CHAUFFEUR')")
    @PostMapping("/createReclamation")
    public ResponseEntity<ReclamationResponse> create(@RequestBody ReclamationRequest request) {
        return ResponseEntity.ok(service.create(request));
    }
}