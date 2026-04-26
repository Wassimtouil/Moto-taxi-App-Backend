package com.example.taximotoapp_backend.Evaluation.controller;

import com.example.taximotoapp_backend.Evaluation.dto.request.EvaluationRequest;
import com.example.taximotoapp_backend.Evaluation.dto.response.EvaluationResponse;
import com.example.taximotoapp_backend.Evaluation.service.EvaluationService;
import com.example.taximotoapp_backend.reclamation.dto.response.ReclamationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/evaluation")
public class EvaluationController {
    private final EvaluationService evaluationService;

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/createEvaluation")
    public ResponseEntity<?> addEvaluation(@RequestBody EvaluationRequest dto) {
        try {
            EvaluationResponse response = evaluationService.ajouterEvaluation(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @PreAuthorize("hasAnyRole('CLIENT','CHAUFFEUR')")
    @GetMapping("/moyenne/{chauffeurId}")
    public ResponseEntity<Double> getMoyenne(@PathVariable Long chauffeurId) {
        return ResponseEntity.ok(
                evaluationService.getMoyenneChauffeur(chauffeurId)
        );
    }
}
