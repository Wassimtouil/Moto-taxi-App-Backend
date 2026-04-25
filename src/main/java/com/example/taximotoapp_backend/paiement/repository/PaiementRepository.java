package com.example.taximotoapp_backend.paiement.repository;

import com.example.taximotoapp_backend.paiement.model.Paiement;

import java.util.Optional;

public interface PaiementRepository {
    Optional<Paiement> findByTrajetId(Long trajetId);
}
