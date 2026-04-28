package com.example.taximotoapp_backend.Evaluation.service;

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
        evaluation.setQuickChoices(dto.getQuickChoices());
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
}
