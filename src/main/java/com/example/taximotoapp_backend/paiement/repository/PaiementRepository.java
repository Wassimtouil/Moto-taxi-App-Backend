package com.example.taximotoapp_backend.paiement.repository;

import com.example.taximotoapp_backend.paiement.model.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaiementRepository extends JpaRepository<Paiement,Long> {
    Optional<Paiement> findByTrajetId(Long trajetId);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.status = 'COMPLETED'")
    Double sumTotalRevenue();

    @org.springframework.data.jpa.repository.Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.status = 'COMPLETED' AND p.trajet.completedAt >= :since")
    Double sumRevenueSince(@org.springframework.data.repository.query.Param("since") java.time.LocalDateTime since);
}
