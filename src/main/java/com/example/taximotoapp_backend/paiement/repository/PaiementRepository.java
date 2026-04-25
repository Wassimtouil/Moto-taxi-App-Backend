package com.example.taximotoapp_backend.paiement.repository;

import com.example.taximotoapp_backend.paiement.model.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaiementRepository extends JpaRepository<Paiement,Long> {
    Optional<Paiement> findByTrajetId(Long trajetId);
}
