package com.example.taximotoapp_backend.Historique.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HistoriqueClientResponse {
    private LocalDateTime dateCourse;
    private String depart;
    private String destination;
    private Double prix;
    private String modePaiement;
    private String statutPaiement;
    private String nomChauffeur;
    private double noteDonnee;
    private String statutTrajet;
    private String encodedPolyline;
}
