package com.example.taximotoapp_backend.paiement.Tarif.controller;

import com.example.taximotoapp_backend.model.enumClass.TarifPeriode;
import com.example.taximotoapp_backend.paiement.Tarif.dto.TarifConfigRequest;
import com.example.taximotoapp_backend.paiement.Tarif.dto.TarifConfigResponse;
import com.example.taximotoapp_backend.paiement.Tarif.service.TarifConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/paiements/tarifs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class TarifConfigController {

    private final TarifConfigService tarifConfigService;

    /**
     * GET /api/admin/paiements/tarifs
     * Retourne les 2 tarifs (JOUR et NUIT)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TarifConfigResponse>> getAllTarifs() {
        return ResponseEntity.ok(tarifConfigService.getAllTarifs());
    }

    /**
     * PUT /api/admin/paiements/tarifs/{periode}
     * periode = JOUR ou NUIT
     * body: { "prixParKm": 1.5 }
     */
    @PutMapping("/{periode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateTarif(
            @PathVariable TarifPeriode periode,
            @RequestBody TarifConfigRequest request) {
        try {
            TarifConfigResponse response = tarifConfigService.updateTarif(periode, request.getPrixParKm());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
