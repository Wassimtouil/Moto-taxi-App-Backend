package com.example.taximotoapp_backend.Evaluation.model;

import com.example.taximotoapp_backend.User.model.Chauffeur;
import com.example.taximotoapp_backend.User.model.Client;
import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.model.enumClass.TripStatus;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
public class Evaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double note;
    private String commentaire;



    private Integer noteConduite;
    private Integer noteVehicule;
    private Integer notePonctualite;
    private Integer noteService;
    private Integer noteExperience;
    private Integer noteComportement;

    private LocalDateTime dateEvaluation;
    @PrePersist
    protected void onCreate(){
        dateEvaluation = LocalDateTime.now();
    }
    @ManyToOne
    private Client client;
    @ManyToOne
    private Chauffeur chauffeur;
    @OneToOne
    private Trajet trajet;

    public Long getId() {
        return id;
    }

    public double getNote() {
        return note;
    }

    public String getCommentaire() {
        return commentaire;
    }



    public LocalDateTime getDateEvaluation() {
        return dateEvaluation;
    }

    public Client getClient() {
        return client;
    }

    public Chauffeur getChauffeur() {
        return chauffeur;
    }

    public Trajet getTrajet() {
        return trajet;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setChauffeur(Chauffeur chauffeur) {
        this.chauffeur = chauffeur;
    }

    public void setTrajet(Trajet trajet) {
        this.trajet = trajet;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }



    public void setNote(double note) {
        this.note = note;
    }

    public Integer getNoteConduite() {
        return noteConduite;
    }

    public void setNoteConduite(Integer noteConduite) {
        this.noteConduite = noteConduite;
    }

    public Integer getNoteVehicule() {
        return noteVehicule;
    }

    public void setNoteVehicule(Integer noteVehicule) {
        this.noteVehicule = noteVehicule;
    }

    public Integer getNotePonctualite() {
        return notePonctualite;
    }

    public void setNotePonctualite(Integer notePonctualite) {
        this.notePonctualite = notePonctualite;
    }

    public Integer getNoteService() {
        return noteService;
    }

    public void setNoteService(Integer noteService) {
        this.noteService = noteService;
    }

    public Integer getNoteExperience() {
        return noteExperience;
    }

    public void setNoteExperience(Integer noteExperience) {
        this.noteExperience = noteExperience;
    }

    public Integer getNoteComportement() {
        return noteComportement;
    }

    public void setNoteComportement(Integer noteComportement) {
        this.noteComportement = noteComportement;
    }
}