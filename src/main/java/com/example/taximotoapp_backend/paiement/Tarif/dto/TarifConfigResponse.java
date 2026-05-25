package com.example.taximotoapp_backend.paiement.Tarif.dto;

import com.example.taximotoapp_backend.model.enumClass.TarifPeriode;

import java.time.LocalDateTime;

public class TarifConfigResponse {
    private TarifPeriode periode;
    private Double prixParKm;
    private LocalDateTime updatedAt;
    private String updatedBy;

    public TarifConfigResponse() {}

    public TarifConfigResponse(TarifPeriode periode, Double prixParKm, LocalDateTime updatedAt, String updatedBy) {
        this.periode = periode;
        this.prixParKm = prixParKm;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public TarifPeriode getPeriode() { return periode; }
    public void setPeriode(TarifPeriode periode) { this.periode = periode; }

    public Double getPrixParKm() { return prixParKm; }
    public void setPrixParKm(Double prixParKm) { this.prixParKm = prixParKm; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
