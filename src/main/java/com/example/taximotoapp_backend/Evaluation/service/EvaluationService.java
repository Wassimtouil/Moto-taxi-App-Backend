package com.example.taximotoapp_backend.Evaluation.service;

import com.example.taximotoapp_backend.Evaluation.Repository.EvaluationRepository;
import com.example.taximotoapp_backend.Evaluation.dto.request.EvaluationRequest;
import com.example.taximotoapp_backend.Evaluation.dto.response.EvaluationResponse;
import com.example.taximotoapp_backend.Evaluation.mapper.EvaluationMapper;
import com.example.taximotoapp_backend.Evaluation.model.Evaluation;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import com.example.taximotoapp_backend.trajet.repository.TrajetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EvaluationService {
    private final TrajetRepository trajetRepository;
    private final EvaluationRepository evaluationRepository;
    private final EvaluationMapper mapper;
    public EvaluationResponse ajouterEvaluation(EvaluationRequest dto,Long cliendId) {
        Trajet trajet = trajetRepository.findById(dto.getTrajetId())
                .orElseThrow(() -> new RuntimeException("Trajet non trouvé"));
        if (!trajet.getClient().getId().equals(cliendId)){
            throw new RuntimeException("User non autorisé");
        }
        if (!trajet.getStatus().equals("TERMINE")) {
            throw new RuntimeException("Trajet non terminé");
        }
        if (evaluationRepository.findByTrajetId(dto.getTrajetId()).isPresent()) {
            throw new RuntimeException("Trajet déjà évalué");
        }
        Evaluation evaluation = new Evaluation();
        evaluation.setNote(dto.getNote());
        evaluation.setCommentaire(dto.getCommentaire());
        evaluation.setClient(trajet.getClient());
        evaluation.setChauffeur(trajet.getChauffeur());
        evaluation.setTrajet(trajet);
        Evaluation saved = evaluationRepository.save(evaluation);
        return mapper.toResponse(saved);
    }
}
