package com.example.taximotoapp_backend.paiement.Tarif.service;

import com.example.taximotoapp_backend.model.enumClass.TarifPeriode;
import com.example.taximotoapp_backend.paiement.Tarif.dto.TarifConfigResponse;
import com.example.taximotoapp_backend.paiement.Tarif.model.TarifConfig;
import com.example.taximotoapp_backend.paiement.Tarif.repository.TarifConfigRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TarifConfigService {

    private final TarifConfigRepository tarifConfigRepository;

    // --- Initialisation des valeurs par défaut au démarrage ---
    @PostConstruct
    public void initDefaultsIfMissing() {
        if (tarifConfigRepository.findByPeriode(TarifPeriode.JOUR).isEmpty()) {
            TarifConfig jour = new TarifConfig();
            jour.setId(1L);
            jour.setPeriode(TarifPeriode.JOUR);
            jour.setPrixParKm(0.8); // valeur par défaut rétrocompatible
            jour.setUpdatedBy("system");
            tarifConfigRepository.save(jour);
        }

        if (tarifConfigRepository.findByPeriode(TarifPeriode.NUIT).isEmpty()) {
            TarifConfig nuit = new TarifConfig();
            nuit.setId(2L);
            nuit.setPeriode(TarifPeriode.NUIT);
            nuit.setPrixParKm(1.2); // tarif nuit majoré
            nuit.setUpdatedBy("system");
            tarifConfigRepository.save(nuit);
        }
    }

    // --- Récupérer les 2 tarifs ---
    public List<TarifConfigResponse> getAllTarifs() {
        return tarifConfigRepository.findAll()
                .stream()
                .map(t -> new TarifConfigResponse(
                        t.getPeriode(),
                        t.getPrixParKm(),
                        t.getUpdatedAt(),
                        t.getUpdatedBy()
                ))
                .collect(Collectors.toList());
    }

    // --- Mettre à jour un tarif (JOUR ou NUIT) ---
    @Transactional
    public TarifConfigResponse updateTarif(TarifPeriode periode, Double prixParKm) {
        if (prixParKm == null || prixParKm <= 0) {
            throw new RuntimeException("Le prix par km doit être un nombre positif");
        }
        String adminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        TarifConfig config = tarifConfigRepository.findByPeriode(periode)
                .orElseThrow(() -> new RuntimeException("Tarif " + periode + " introuvable"));
        config.setPrixParKm(prixParKm);
        config.setUpdatedBy(adminEmail);
        tarifConfigRepository.save(config);
        return new TarifConfigResponse(
                config.getPeriode(),
                config.getPrixParKm(),
                config.getUpdatedAt(),
                config.getUpdatedBy()
        );
    }
}

