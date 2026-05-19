package com.example.taximotoapp_backend.Evaluation.service;

import com.example.taximotoapp_backend.Admin.dto.AdminEvaluationDto;
import com.example.taximotoapp_backend.Admin.dto.AdminEvaluationStatsDto;
import com.example.taximotoapp_backend.Evaluation.Repository.EvaluationRepository;
import com.example.taximotoapp_backend.Evaluation.dto.request.EvaluationRequest;
import com.example.taximotoapp_backend.Evaluation.dto.response.EvaluationResponse;
import com.example.taximotoapp_backend.Evaluation.mapper.EvaluationMapper;
import com.example.taximotoapp_backend.Evaluation.model.Evaluation;
import com.example.taximotoapp_backend.User.model.Chauffeur;
import com.example.taximotoapp_backend.User.repository.ChauffeurRepository;
import com.example.taximotoapp_backend.model.enumClass.TripStatus;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import com.example.taximotoapp_backend.trajet.repository.TrajetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationService {
    private final TrajetRepository trajetRepository;
    private final EvaluationRepository evaluationRepository;
    private final EvaluationMapper mapper;
    private final ChauffeurRepository chauffeurRepository;

    public EvaluationResponse ajouterEvaluation(EvaluationRequest dto) {
        Trajet trajet = trajetRepository.findById(dto.getTrajetId())
                .orElseThrow(() -> new RuntimeException("Trajet non trouvé"));
        if (!trajet.getStatus().equals(TripStatus.Completed)) {
            throw new RuntimeException("Trajet non terminé");
        }
        if (evaluationRepository.findByTrajetId(dto.getTrajetId()).isPresent()) {
            throw new RuntimeException("Trajet déjà évalué");
        }
        Evaluation evaluation = new Evaluation();
        evaluation.setNote(dto.getNote());
        evaluation.setCommentaire(dto.getCommentaire());
        evaluation.setNoteConduite(dto.getNoteConduite());
        evaluation.setNoteVehicule(dto.getNoteVehicule());
        evaluation.setNotePonctualite(dto.getNotePonctualite());
        evaluation.setNoteService(dto.getNoteService());
        evaluation.setNoteExperience(dto.getNoteExperience());
        evaluation.setNoteComportement(dto.getNoteComportement());
        evaluation.setClient(trajet.getClient());
        evaluation.setChauffeur(trajet.getChauffeur());
        evaluation.setTrajet(trajet);

        // Save evaluation first
        Evaluation saved = evaluationRepository.save(evaluation);

        // Then calculate and update the average
        Double moyenne = evaluationRepository.getMoyenne(trajet.getChauffeur().getId());
        Chauffeur chauffeur = trajet.getChauffeur();
        chauffeur.setNoteMoyenne(moyenne != null ? moyenne : 0.0);
        chauffeurRepository.save(chauffeur);

        return mapper.toResponse(saved);
    }

    public double getMoyenneChauffeur(Long chauffeurId) {
        Chauffeur chauffeur=chauffeurRepository.findById(chauffeurId).orElseThrow(()->new RuntimeException("Chauffeur not found"));
        return chauffeur.getNoteMoyenne() != null ? chauffeur.getNoteMoyenne() : 0.0;
    }

    // --- Admin specific services ---

    public List<AdminEvaluationDto> getAllEvaluationsForAdmin() {
        return evaluationRepository.findAll().stream()
                .map(mapper::toAdminDto)
                .collect(Collectors.toList());
    }

    public void deleteEvaluationForAdmin(Long id) {
        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluation non trouvée"));

        Chauffeur chauffeur = evaluation.getChauffeur();
        evaluationRepository.delete(evaluation);

        // Recalculate average for chauffeur
        if (chauffeur != null) {
            Double moyenne = evaluationRepository.getMoyenne(chauffeur.getId());
            chauffeur.setNoteMoyenne(moyenne != null ? moyenne : 0.0);
            chauffeurRepository.save(chauffeur);
        }
    }

    public AdminEvaluationStatsDto getGlobalEvaluationStats() {
        List<Evaluation> evaluations = evaluationRepository.findAll();

        long totalEvaluations = evaluations.size();
        double globalAverage = evaluations.stream()
                .mapToDouble(Evaluation::getNote)
                .average()
                .orElse(0.0);

        // Round global average to 2 decimal places
        globalAverage = Math.round(globalAverage * 100.0) / 100.0;

        // Rating distribution
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }
        for (Evaluation e : evaluations) {
            int roundedNote = (int) Math.round(e.getNote());
            if (roundedNote >= 1 && roundedNote <= 5) {
                distribution.put(roundedNote, distribution.get(roundedNote) + 1);
            }
        }

        // Criteria averages
        Map<String, Double> criteriaAverages = new HashMap<>();

        criteriaAverages.put("Conduite", roundToTwo(evaluations.stream().filter(e -> e.getNoteConduite() != null).mapToInt(Evaluation::getNoteConduite).average().orElse(0.0)));
        criteriaAverages.put("Véhicule", roundToTwo(evaluations.stream().filter(e -> e.getNoteVehicule() != null).mapToInt(Evaluation::getNoteVehicule).average().orElse(0.0)));
        criteriaAverages.put("Ponctualité", roundToTwo(evaluations.stream().filter(e -> e.getNotePonctualite() != null).mapToInt(Evaluation::getNotePonctualite).average().orElse(0.0)));
        criteriaAverages.put("Service", roundToTwo(evaluations.stream().filter(e -> e.getNoteService() != null).mapToInt(Evaluation::getNoteService).average().orElse(0.0)));
        criteriaAverages.put("Expérience", roundToTwo(evaluations.stream().filter(e -> e.getNoteExperience() != null).mapToInt(Evaluation::getNoteExperience).average().orElse(0.0)));
        criteriaAverages.put("Comportement", roundToTwo(evaluations.stream().filter(e -> e.getNoteComportement() != null).mapToInt(Evaluation::getNoteComportement).average().orElse(0.0)));

        return AdminEvaluationStatsDto.builder()
                .globalAverage(globalAverage)
                .totalEvaluations(totalEvaluations)
                .ratingDistribution(distribution)
                .criteriaAverages(criteriaAverages)
                .build();
    }

    private double roundToTwo(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}

