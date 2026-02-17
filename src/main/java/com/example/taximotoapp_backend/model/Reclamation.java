package com.example.taximotoapp_backend.model;

import jakarta.persistence.*;
import org.springframework.context.annotation.EnableMBeanExport;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Reclamation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String objet;
    private String message;
    private LocalDate dateReclamation;
    @PrePersist
    void onCreate (){
        dateReclamation=LocalDate.now();
    }
    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    public Long getId() {
        return id;
    }

    public String getObjet() {
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

    public void setObjet(String objet) {
        this.objet = objet;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
