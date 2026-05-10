package com.example.taximotoapp_backend.Historique.service;

import com.example.taximotoapp_backend.Historique.dto.response.HistoriqueChauffeurResponse;
import com.example.taximotoapp_backend.Historique.dto.response.HistoriqueClientResponse;
import com.example.taximotoapp_backend.Historique.mapper.HistoriqueMapper;
import com.example.taximotoapp_backend.User.model.Chauffeur;
import com.example.taximotoapp_backend.User.model.Client;
import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.User.repository.ChauffeurRepository;
import com.example.taximotoapp_backend.User.repository.ClientRepository;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import com.example.taximotoapp_backend.trajet.repository.TrajetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

import com.example.taximotoapp_backend.Historique.dto.response.TransactionResponse;
import com.example.taximotoapp_backend.paiement.model.Wallet;
import com.example.taximotoapp_backend.paiement.repository.TransactionRepository;
import com.example.taximotoapp_backend.paiement.repository.WalletRepository;

import com.example.taximotoapp_backend.trajet.dto.response.TrajetResponse;
import com.example.taximotoapp_backend.trajet.mapper.TrajetMapper;

@Service
@RequiredArgsConstructor
public class HistoriqueService {
    private final TrajetRepository trajetRepository;
    private final ClientRepository clientRepository;
    private final ChauffeurRepository chauffeurRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final HistoriqueMapper mapper;
    private final TrajetMapper trajetMapper;

    public List<TrajetResponse> getTrajetHistoryClient() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User client = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Client not found"));
        return trajetRepository.findByClientIdOrderByRequestedAtDesc(client.getId())
                .stream()
                .map(trajetMapper::toDTO)
                .toList();
    }

    public List<TrajetResponse> getTrajetHistoryChauffeur() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User driver = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Driver not found"));
        return trajetRepository.findByChauffeurIdOrderByRequestedAtDesc(driver.getId())
                .stream()
                .map(trajetMapper::toDTO)
                .toList();
    }

    public List<HistoriqueClientResponse> getHistoriqueClient() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User introuvable"));
        if (!(user instanceof Client client)) {
            throw new RuntimeException("Accès refusé : utilisateur non client");
        }
        return trajetRepository.findByClientIdOrderByRequestedAtDesc(client.getId())
                .stream()
                .map(mapper::toHistoriqueClientResponse)
                .toList();
    }

    public List<HistoriqueChauffeurResponse> getHistoriqueChauffeur() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User introuvable"));
        if (!(user instanceof Chauffeur chauffeur)) {
            throw new RuntimeException("Accès refusé : utilisateur non Chauffeur");
        }
        return trajetRepository.findByChauffeurIdOrderByRequestedAtDesc(chauffeur.getId())
                .stream()
                .map(mapper::toHistoriqueChauffeurResponse)
                .toList();
    }

    public List<TransactionResponse> getTransactions() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User introuvable"));
        java.util.Optional<Wallet> walletOpt = walletRepository.findByUser(user);

        List<TransactionResponse> walletTx;
        if (walletOpt.isPresent()) {
            walletTx = transactionRepository.findByWalletIdOrderByTimestampDesc(walletOpt.get().getId())
                    .stream()
                    .map(tx -> new TransactionResponse(
                            tx.getId(),
                            tx.getAmount(),
                            tx.getType().name(),
                            tx.getStatus().name(),
                            tx.getDescription(),
                            tx.getTimestamp()
                    ))
                    .toList();
        } else {
            walletTx = java.util.Collections.emptyList();
        }


        List<Trajet> userTrajets;
        boolean isClient = false;
        if (user instanceof Client client) {
            userTrajets = client.getTrajets();
            isClient = true;
        } else if (user instanceof Chauffeur chauffeur) {
            userTrajets = chauffeur.getTrajets();
        } else {
            userTrajets = java.util.Collections.emptyList();
        }

        final boolean finalIsClient = isClient;
        List<TransactionResponse> cashTx = userTrajets.stream()
                .filter(t -> t.getPaiement() != null && t.getPaiement().getType() == com.example.taximotoapp_backend.model.enumClass.PaiementType.CASH)
                .map(t -> new TransactionResponse(
                        t.getPaiement().getId(),
                        t.getPaiement().getMontant(),
                        finalIsClient ? "PAYMENT" : "EARNING",
                        t.getPaiement().getStatus().name(),
                        (finalIsClient ? "Paiement" : "Gain") + " en espÃ¨ces (Trajet #" + t.getId() + ")",
                        t.getPaiement().getDatePaiement() != null ? t.getPaiement().getDatePaiement() : t.getRequestedAt()
                ))
                .toList();

        List<TransactionResponse> allTx = new java.util.ArrayList<>();
        allTx.addAll(walletTx);
        allTx.addAll(cashTx);
        allTx.sort((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp()));
        return allTx;
    }

}

