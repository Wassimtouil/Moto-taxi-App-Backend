package com.example.taximotoapp_backend.reclamation.model;

import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.model.enumClass.ReclamationStatus;
import com.example.taximotoapp_backend.model.enumClass.ReclamationsType;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Reclamation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ReclamationsType objet;

    private String message;

    @Enumerated(EnumType.STRING)
    private ReclamationStatus reclamationStatus;

    private LocalDate dateReclamation;
    @PrePersist
    void onCreate (){
        dateReclamation=LocalDate.now();
        reclamationStatus=ReclamationStatus.EN_ATTENTE;
    }
    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    private String adminResponse;
    public Long getId() {
        return id;
    }

    public ReclamationsType getObjet() {
        return objet;
    }

    public String getMessage() {
        return message;
    }

    public LocalDate getDateReclamation() {
        return dateReclamation;
    }

    public User getUser() {
        return user;
    }

    public String getAdminResponse() { return adminResponse; }

    public ReclamationStatus getReclamationStatus() {
        return reclamationStatus;
    }

    public void setObjet(ReclamationsType objet) {
        this.objet = objet;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUser(User user) {
        this.user = user;
    }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }

    public void setReclamationStatus(ReclamationStatus reclamationStatus) {
        this.reclamationStatus = reclamationStatus;
    }
}
