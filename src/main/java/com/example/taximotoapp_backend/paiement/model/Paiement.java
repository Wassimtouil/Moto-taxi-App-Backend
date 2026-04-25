package com.example.taximotoapp_backend.paiement.model;

import com.example.taximotoapp_backend.model.enumClass.PaiementStatus;
import com.example.taximotoapp_backend.model.enumClass.PaiementType;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Paiement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double montant;
    @Enumerated(EnumType.STRING)
    private PaiementType type;
    @Enumerated(EnumType.STRING)
    private PaiementStatus status;
    private LocalDateTime datePaiement;
    @OneToOne
    @JoinColumn(name = "trajet_id", nullable = false, unique = true)
    private Trajet trajet;
    @PrePersist
    public void onCreate() {
        this.datePaiement = LocalDateTime.now();
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public Long getId() {
        return id;
    }

    public void setStatus(PaiementStatus status) {
        this.status = status;
    }

    public void setType(PaiementType type) {
        this.type = type;
    }

    public void setTrajet(Trajet trajet) {
        this.trajet = trajet;
    }

    public Double getMontant() {
        return montant;
    }

    public PaiementType getType() {
        return type;
    }

    public PaiementStatus getStatus() {
        return status;
    }

    public LocalDateTime getDatePaiement() {
        return datePaiement;
    }

    public Trajet getTrajet() {
        return trajet;
    }
}
