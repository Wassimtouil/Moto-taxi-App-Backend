package com.example.taximotoapp_backend.Historique.controller;


import com.example.taximotoapp_backend.Historique.dto.response.HistoriqueChauffeurResponse;
import com.example.taximotoapp_backend.Historique.dto.response.HistoriqueClientResponse;
import com.example.taximotoapp_backend.Historique.service.HistoriqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/historique")
public class HistoriqueController {
    private final HistoriqueService historiqueService;
    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/client")
    public ResponseEntity<?> getHistoriqueClient() {
        try {
            List<HistoriqueClientResponse> result = historiqueService.getHistoriqueClient();
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération de l'historique client");
        }
    }
    @PreAuthorize("hasRole('CHAUFFEUR')")
    @GetMapping("/chauffeur")
    public ResponseEntity<?> getHistoriqueChauffeur() {
        try {
            List<HistoriqueChauffeurResponse> result = historiqueService.getHistoriqueChauffeur();
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération de l'historique chauffeur");
        }
    }
}
