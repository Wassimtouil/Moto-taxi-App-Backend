package com.example.taximotoapp_backend.reclamation.controller;

import com.example.taximotoapp_backend.reclamation.dto.request.ReclamationRequest;
import com.example.taximotoapp_backend.reclamation.dto.response.ReclamationResponse;
import com.example.taximotoapp_backend.reclamation.dto.response.ReclamationResponseAdmin;
import com.example.taximotoapp_backend.reclamation.service.ReclamationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reclamation")
public class ReclamationController {

    private final ReclamationService service;

    @PreAuthorize("hasAnyRole('CLIENT','CHAUFFEUR')")
    @PostMapping("/createReclamation")
    public ResponseEntity<?> create(@RequestBody ReclamationRequest request) {
        try {
            ReclamationResponse response = service.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyRole('CLIENT','CHAUFFEUR')")
    @GetMapping("/getAllReclamation")
    public ResponseEntity<?> getAll() {
        try {
            List<ReclamationResponse> response = service.getMyReclamations();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyRole('CLIENT','CHAUFFEUR')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReclamation(
            @PathVariable Long id,
            @RequestBody ReclamationRequest request
    ) {
        try {
            ReclamationResponse response = service.update(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyRole('CLIENT','CHAUFFEUR')")
    @DeleteMapping("/delete/{id}")
    public void deleteReclamation(@PathVariable Long id){
        service.delete(id);
    }

    // --- ADMIN ENDPOINTS ---

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReclamationResponseAdmin>> getAllForAdmin() {
        return ResponseEntity.ok(service.getAllForAdmin());
    }

    @PostMapping("/admin/{id}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReclamationResponseAdmin> replyToReclamation(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String responseText = body.get("response");
        return ResponseEntity.ok(service.reply(id, responseText));
    }

    @DeleteMapping("/admin/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteByAdmin(@PathVariable Long id) {
        service.deleteByAdmin(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/pending-count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getPendingCount() {
        return ResponseEntity.ok(service.countPendingReclamations());
    }
}