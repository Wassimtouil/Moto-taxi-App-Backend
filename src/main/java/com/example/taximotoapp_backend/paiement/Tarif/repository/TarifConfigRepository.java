package com.example.taximotoapp_backend.paiement.Tarif.repository;

import com.example.taximotoapp_backend.model.enumClass.TarifPeriode;
import com.example.taximotoapp_backend.paiement.Tarif.model.TarifConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TarifConfigRepository extends JpaRepository<TarifConfig,Long> {
    Optional<TarifConfig> findByPeriode(TarifPeriode periode);

}
