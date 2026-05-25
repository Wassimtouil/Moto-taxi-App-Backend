package com.example.taximotoapp_backend.paiement.Tarif.model;

import com.example.taximotoapp_backend.model.enumClass.TarifPeriode;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Table(name = "tarif_config")
@Entity
public class TarifConfig {

    @Id
    private Long id; // 1 = JOUR, 2 = NUIT

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 10)
    private TarifPeriode periode;

    @Column(name = "prix_par_km", nullable = false)
    private Double prixParKm;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PreUpdate
    @PrePersist
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getters ---

    public Long getId() {
        return id;
    }

    public TarifPeriode getPeriode() {
        return periode;
    }

    public Double getPrixParKm() {
        return prixParKm;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    // --- Setters ---

    public void setId(Long id) {
        this.id = id;
    }

    public void setPeriode(TarifPeriode periode) {
        this.periode = periode;
    }

    public void setPrixParKm(Double prixParKm) {
        this.prixParKm = prixParKm;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
