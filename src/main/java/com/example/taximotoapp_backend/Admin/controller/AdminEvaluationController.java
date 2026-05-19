package com.example.taximotoapp_backend.Admin.controller;

import com.example.taximotoapp_backend.Admin.dto.AdminEvaluationDto;
import com.example.taximotoapp_backend.Admin.dto.AdminEvaluationStatsDto;
import com.example.taximotoapp_backend.Evaluation.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/evaluation")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminEvaluationController {

    private final EvaluationService evaluationService;

    /**
     * Récupère toutes les évaluations pour l'admin
     */
    @GetMapping("/all")
    public ResponseEntity<List<AdminEvaluationDto>> getAllEvaluations() {
        return ResponseEntity.ok(evaluationService.getAllEvaluationsForAdmin());
    }

    /**
     * Récupère les statistiques globales d'évaluations
     */
    @GetMapping("/stats")
    public ResponseEntity<AdminEvaluationStatsDto> getGlobalStats() {
        return ResponseEntity.ok(evaluationService.getGlobalEvaluationStats());
    }

    /**
     * Supprime une évaluation par l'admin
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteEvaluation(@PathVariable Long id) {
        evaluationService.deleteEvaluationForAdmin(id);
        return ResponseEntity.ok().build();
    }
}
