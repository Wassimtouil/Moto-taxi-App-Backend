package com.example.taximotoapp_backend.Admin.controller;

import com.example.taximotoapp_backend.reclamation.dto.response.ReclamationResponseAdmin;
import com.example.taximotoapp_backend.reclamation.service.ReclamationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reclamation")
@RequiredArgsConstructor
public class AdminReclamationController {

    private final ReclamationService service;

    /**
     * Récupère toutes les réclamations pour l'admin
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllForAdmin() {
        return ResponseEntity.ok(service.getAllForAdmin());
    }

    /**
     * Répond à une réclamation
     */
    @PostMapping("/{id}/reply")
    public ResponseEntity<ReclamationResponseAdmin> replyToReclamation(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String responseText = body.get("response");
        return ResponseEntity.ok(service.reply(id, responseText));
    }

    /**
     * Supprime une réclamation par l'admin
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteByAdmin(@PathVariable Long id) {
        service.deleteByAdmin(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Récupère le nombre de réclamations en attente
     */
    @GetMapping("/pending-count")
    public ResponseEntity<Long> getPendingCount() {
        return ResponseEntity.ok(service.countPendingReclamations());
    }
    @GetMapping("/stats-by-type")
    public ResponseEntity<Map<String, Long>> getStatsByType() {
        return ResponseEntity.ok(service.getStatsByType());
    }
}
