package com.example.taximotoapp_backend.trajet.controller;


import com.example.taximotoapp_backend.trajet.dto.response.ChauffeurStatResponse;
import com.example.taximotoapp_backend.trajet.dto.response.TrajetResponse;
import com.example.taximotoapp_backend.trajet.service.TrajetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/trajets")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTrajetController {

    private final TrajetService trajetService;

    /**
     * Récupère les statistiques de performance de tous les chauffeurs
     */
    @GetMapping("/chauffeurs/stats")
    public ResponseEntity<List<ChauffeurStatResponse>> getAllChauffeurStats() {
        return ResponseEntity.ok(trajetService.getAllChauffeurStats());
    }

    /**
     * Récupère l'historique des trajets d'un chauffeur spécifique
     */
    @GetMapping("/chauffeurs/{id}")
    public ResponseEntity<List<TrajetResponse>> getChauffeurTrajets(@PathVariable Long id) {
        return ResponseEntity.ok(trajetService.getTrajetsByChauffeurId(id));
    }
}