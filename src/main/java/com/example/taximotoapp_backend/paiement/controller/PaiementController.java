package com.example.taximotoapp_backend.paiement.controller;
import com.example.taximotoapp_backend.paiement.dto.response.PaiementResponse;
import com.example.taximotoapp_backend.paiement.service.PaiementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/paiement")
@RequiredArgsConstructor
public class PaiementController {
    private final PaiementService paiementService;

    @GetMapping("/{trajetId}")
    public ResponseEntity<PaiementResponse> getByTrajet(@PathVariable Long trajetId) {
        return ResponseEntity.ok(paiementService.getByTrajet(trajetId));
    }

    @PostMapping("/{paiementId}/confirm")
    public ResponseEntity<PaiementResponse> confirmerPaiement(@PathVariable Long paiementId) {
        return ResponseEntity.ok(paiementService.confirmerPaiement(paiementId));
    }
}
