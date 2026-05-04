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
        Trajet trajet = trajetRepository.findById(dto.getTrajetId())
                .orElseThrow(() -> new RuntimeException("Trajet not found"));

        // Fix: Check if payment exists correctly
        if (paiementRepository.findByTrajetId(trajet.getId()).isPresent()) {
            throw new RuntimeException("Paiement déjà existe pour ce trajet");
        }

        Paiement paiement = new Paiement();
        paiement.setMontant(dto.getMontant());
        paiement.setType(dto.getType());
        paiement.setTrajet(trajet);

        // Use user's instruction: simulated payments are marked PAYE
        paiement.setStatus(PaiementStatus.PAYE);

        paiementRepository.save(paiement);
        return mapper.toResponse(paiement);
    }

    @Transactional
    public void createFromTrajet(Trajet trajet) {
        // If payment already exists, just return or update
        if (paiementRepository.findByTrajetId(trajet.getId()).isPresent()) {
            return;
        }

        Paiement paiement = new Paiement();
        paiement.setMontant(trajet.getPrice());
        paiement.setType(trajet.getPaymentMethod() != null ? trajet.getPaymentMethod() : PaiementType.CASH);
        paiement.setTrajet(trajet);

        // User instruction: Mark as PAYE when trip starts
        paiement.setStatus(PaiementStatus.PAYE);

        paiementRepository.save(paiement);
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
