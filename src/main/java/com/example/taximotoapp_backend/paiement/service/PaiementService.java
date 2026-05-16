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

import com.example.taximotoapp_backend.User.model.Chauffeur;
import com.example.taximotoapp_backend.User.model.Client;
import com.example.taximotoapp_backend.paiement.dto.response.ApiResponse;
import com.example.taximotoapp_backend.paiement.model.Wallet;
import com.example.taximotoapp_backend.model.enumClass.TransactionStatus;
import com.example.taximotoapp_backend.model.enumClass.TransactionType;
import com.example.taximotoapp_backend.paiement.repository.WalletRepository;

@Service
@RequiredArgsConstructor
public class PaiementService {
    private final PaiementRepository paiementRepository;
    private final TrajetRepository trajetRepository;
    private final PaiementMapper mapper;
    private final WalletService walletService;
    private final WalletRepository walletRepository;

    @Transactional
    public ApiResponse processTripPayment(Long tripId) {
        Trajet trajet = trajetRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        Paiement paiement = paiementRepository.findByTrajetId(tripId).orElse(null);

        if (paiement != null && paiement.getStatus() != PaiementStatus.PAYE) {
            Client client = trajet.getClient();
            Chauffeur driver = trajet.getChauffeur();
            Double price = paiement.getMontant();

            if (paiement.getType() == PaiementType.ONLINE) {
                Wallet clientWallet = walletService.getOrCreateWallet(client.getId());

                if (clientWallet.getBalance() < price) {
                    throw new RuntimeException("Insufficient funds"); // Will be mapped to 402 in controller
                }

                // Deduct from client
                clientWallet.setBalance(clientWallet.getBalance() - price);
                walletRepository.save(clientWallet);
                String destination = trajet.getTrajetLocation() != null ? trajet.getTrajetLocation().getDestinationAddress() : "";
                String payDesc = "Paiement course → " + (destination != null && !destination.isEmpty() ? destination : "Trajet #" + tripId);
                walletService.createTransaction(clientWallet, price, TransactionType.PAYMENT, TransactionStatus.COMPLETED, payDesc);

                // Add to driver
                if (driver != null) {
                    Wallet driverWallet = walletService.getOrCreateWallet(driver.getId());
                    driverWallet.setBalance(driverWallet.getBalance() + price);
                    walletRepository.save(driverWallet);
                    String dest = trajet.getTrajetLocation() != null ? trajet.getTrajetLocation().getDestinationAddress() : "";
                    String earnDesc = "Gain course → " + (dest != null && !dest.isEmpty() ? dest : "Trajet #" + tripId);
                    walletService.createTransaction(driverWallet, price, TransactionType.DEPOSIT, TransactionStatus.COMPLETED, earnDesc);
                }
            } else if (paiement.getType() == PaiementType.CASH) {
                // For cash payments, just update the driver's cash tracking balance
                if (driver != null) {
                    Wallet driverWallet = walletService.getOrCreateWallet(driver.getId());
                    Double currentCash = driverWallet.getCashBalance() != null ? driverWallet.getCashBalance() : 0.0;
                    driverWallet.setCashBalance(currentCash + price);
                    walletRepository.save(driverWallet);
                    // We DO NOT record a Transaction here for CASH. It is dynamically synthesized in HistoriqueService.
                }
            }

            paiement.setStatus(PaiementStatus.PAYE);
            paiementRepository.save(paiement);
        }

        return new ApiResponse(true, "Payment processed successfully");
    }

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
