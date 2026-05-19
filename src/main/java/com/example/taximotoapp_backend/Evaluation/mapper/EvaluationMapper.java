package com.example.taximotoapp_backend.Evaluation.mapper;

import com.example.taximotoapp_backend.Admin.dto.AdminEvaluationDto;
import com.example.taximotoapp_backend.Evaluation.dto.response.EvaluationResponse;
import com.example.taximotoapp_backend.Evaluation.model.Evaluation;
import org.springframework.stereotype.Component;

@Component
public class EvaluationMapper {
    public EvaluationResponse toResponse(Evaluation evaluation) {
        EvaluationResponse evaluationResponse=new EvaluationResponse();
        evaluationResponse.setId(evaluation.getId());
        evaluationResponse.setDateEvaluation(evaluation.getDateEvaluation());
        evaluationResponse.setNote(evaluation.getNote());
        evaluationResponse.setCommentaire(evaluation.getCommentaire());
        evaluationResponse.setClientNom(evaluation.getClient().getFullName());
        evaluationResponse.setChauffeurNom(evaluation.getChauffeur().getFullName());
        evaluationResponse.setNoteConduite(evaluation.getNoteConduite());
        evaluationResponse.setNoteVehicule(evaluation.getNoteVehicule());
        evaluationResponse.setNotePonctualite(evaluation.getNotePonctualite());
        evaluationResponse.setNoteService(evaluation.getNoteService());
        evaluationResponse.setNoteExperience(evaluation.getNoteExperience());
        evaluationResponse.setNoteComportement(evaluation.getNoteComportement());
        return evaluationResponse;
    }

    public AdminEvaluationDto toAdminDto(Evaluation evaluation) {
        if (evaluation == null) return null;

        return AdminEvaluationDto.builder()
                .id(evaluation.getId())
                .note(evaluation.getNote())
                .commentaire(evaluation.getCommentaire())
                .dateEvaluation(evaluation.getDateEvaluation())
                .clientId(evaluation.getClient() != null ? evaluation.getClient().getId() : null)
                .clientName(evaluation.getClient() != null ? evaluation.getClient().getFullName() : "N/A")
                .clientEmail(evaluation.getClient() != null ? evaluation.getClient().getEmail() : "N/A")
                .chauffeurId(evaluation.getChauffeur() != null ? evaluation.getChauffeur().getId() : null)
                .chauffeurName(evaluation.getChauffeur() != null ? evaluation.getChauffeur().getFullName() : "N/A")
                .chauffeurEmail(evaluation.getChauffeur() != null ? evaluation.getChauffeur().getEmail() : "N/A")
                .trajetId(evaluation.getTrajet() != null ? evaluation.getTrajet().getId() : null)
                .pickupAddress(evaluation.getTrajet() != null && evaluation.getTrajet().getTrajetLocation() != null ? evaluation.getTrajet().getTrajetLocation().getPickupAddress() : "N/A")
                .destinationAddress(evaluation.getTrajet() != null && evaluation.getTrajet().getTrajetLocation() != null ? evaluation.getTrajet().getTrajetLocation().getDestinationAddress() : "N/A")
                .noteConduite(evaluation.getNoteConduite())
                .noteVehicule(evaluation.getNoteVehicule())
                .notePonctualite(evaluation.getNotePonctualite())
                .noteService(evaluation.getNoteService())
                .noteExperience(evaluation.getNoteExperience())
                .noteComportement(evaluation.getNoteComportement())
                .build();
    }
}

