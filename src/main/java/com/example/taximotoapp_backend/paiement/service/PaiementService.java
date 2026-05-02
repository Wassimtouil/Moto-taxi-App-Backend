package com.example.taximotoapp_backend.paiement.service;

import com.example.taximotoapp_backend.model.enumClass.PaiementStatus;
import com.example.taximotoapp_backend.model.enumClass.PaiementType;
import com.example.taximotoapp_backend.paiement.dto.request.PaiementRequest;
import com.example.taximotoapp_backend.paiement.dto.response.PaiementResponse;
import com.example.taximotoapp_backend.paiement.mapper.PaiementMapper;
import com.example.taximotoapp_backend.paiement.model.Paiement;
import com.example.taximotoapp_backend.paiement.repository.PaiementRepository;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import com.example.taximotoapp_backend.trajet.repository.TrajetRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaiementService {
    private final PaiementRepository paiementRepository;
    private final TrajetRepository trajetRepository;
    private final PaiementMapper mapper;

    // Créer paiement
    @Transactional
    public PaiementResponse createPaiement(PaiementRequest dto) {
        Trajet trajet = trajetRepository.findById(dto.getTrajetId()).orElseThrow(() -> new RuntimeException("Trajet not found"));
        // Vérifier si paiement existe déjà
        Paiement paiementAnc=paiementRepository.findByTrajetId(trajet.getId()).orElseThrow(()-> new RuntimeException("Paiement déjà existe pour ce trajet"));

        Paiement paiement = new Paiement();
        paiement.setMontant(dto.getMontant());
        paiement.setType(dto.getType());
        paiement.setTrajet(trajet);

        paiement.setStatus(PaiementStatus.EN_ATTENTE);

        paiementRepository.save(paiement);
        return mapper.toResponse(paiement);
    }

    // Valider paiement (cash)
    @Transactional
    public PaiementResponse confirmerPaiement(Long paiementId) {
        Paiement paiement = paiementRepository.findById(paiementId).orElseThrow(() -> new RuntimeException("Paiement not found"));
        paiement.setStatus(PaiementStatus.PAYE);
        return mapper.toResponse(paiement);
    }

    // Get paiement par trajet
    public PaiementResponse getByTrajet(Long trajetId) {
        Paiement paiement = paiementRepository.findByTrajetId(trajetId)
                .orElseThrow(() -> new RuntimeException("Paiement not found"));
        return mapper.toResponse(paiement);
    }

}
