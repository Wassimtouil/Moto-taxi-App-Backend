package com.example.taximotoapp_backend.Evaluation.mapper;

import com.example.taximotoapp_backend.Evaluation.dto.response.EvaluationResponse;
import com.example.taximotoapp_backend.Evaluation.model.Evaluation;
import com.example.taximotoapp_backend.reclamation.dto.response.ReclamationResponse;
import com.example.taximotoapp_backend.reclamation.model.Reclamation;
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
}
