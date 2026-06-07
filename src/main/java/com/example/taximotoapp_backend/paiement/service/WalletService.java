package com.example.taximotoapp_backend.paiement.service;

import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.paiement.dto.request.DepositRequest;
import com.example.taximotoapp_backend.paiement.dto.request.WithdrawRequest;
import com.example.taximotoapp_backend.paiement.dto.response.ApiResponse;
import com.example.taximotoapp_backend.Historique.dto.response.TransactionResponse;
import com.example.taximotoapp_backend.paiement.dto.response.WalletResponse;
import com.example.taximotoapp_backend.paiement.model.Card;
import com.example.taximotoapp_backend.paiement.model.TransactionCard;
import com.example.taximotoapp_backend.paiement.model.TransactionPaiement;
import com.example.taximotoapp_backend.paiement.model.Wallet;
import com.example.taximotoapp_backend.model.enumClass.TransactionStatus;
import com.example.taximotoapp_backend.model.enumClass.TransactionType;
import com.example.taximotoapp_backend.paiement.repository.CardRepository;
import com.example.taximotoapp_backend.paiement.repository.TransactionCardRepository;
import com.example.taximotoapp_backend.paiement.repository.TransactionPaiementRepository;
import com.example.taximotoapp_backend.paiement.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final TransactionCardRepository transactionCardRepository;
    private final TransactionPaiementRepository transactionPaiementRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;

    public WalletResponse getWalletByUserId(Long userId) {
        Wallet wallet = getOrCreateWallet(userId);
        return mapToResponse(wallet);
    }

    public Double getBalance(Long userId) {
        Wallet wallet = getOrCreateWallet(userId);
        return wallet.getBalance();
    }

    @Transactional
    public ApiResponse deposit(Long userId, DepositRequest request) {
        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        Card card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (!card.getUser().getId().equals(userId)) {
            throw new RuntimeException("Card does not belong to user");
        }

        Wallet wallet = getOrCreateWallet(userId);
        wallet.setBalance(wallet.getBalance() + request.getAmount());
        walletRepository.save(wallet);

        createCardTransaction(wallet, request.getAmount(), TransactionType.DEPOSIT, TransactionStatus.COMPLETED, "Rechargement par carte •••• " + card.getLast4Digits());
        return new ApiResponse(true, "Deposit successful");
    }

    @Transactional
    public ApiResponse withdraw(Long userId, WithdrawRequest request) {
        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        Wallet wallet = getOrCreateWallet(userId);

        if (wallet.getBalance() < request.getAmount()) {
            throw new RuntimeException("Insufficient balance");
        }

        Card card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (!card.getUser().getId().equals(userId)) {
            throw new RuntimeException("Card does not belong to user");
        }

        wallet.setBalance(wallet.getBalance() - request.getAmount());
        walletRepository.save(wallet);

        createCardTransaction(wallet, request.getAmount(), TransactionType.WITHDRAWAL, TransactionStatus.COMPLETED, "Retrait vers carte •••• " + card.getLast4Digits());
        return new ApiResponse(true, "Withdrawal successful");
    }

    @Transactional
    public TransactionCard createCardTransaction(Wallet wallet, Double amount, TransactionType type, TransactionStatus status, String description) {
        TransactionCard tx = new TransactionCard();
        tx.setWallet(wallet);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setStatus(status);
        tx.setDescription(description);
        return transactionCardRepository.save(tx);
    }

    @Transactional
    public TransactionPaiement createPaiementTransaction(Wallet wallet, Double amount, String type, String status, String description, com.example.taximotoapp_backend.paiement.model.Paiement paiement) {
        TransactionPaiement tx = new TransactionPaiement();
        tx.setWallet(wallet);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setStatus(status);
        tx.setDescription(description);
        tx.setPaiement(paiement);
        return transactionPaiementRepository.save(tx);
    }

    public Wallet getOrCreateWallet(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return walletRepository.findByUser(user).orElseGet(() -> {
            Wallet newWallet = new Wallet();
            newWallet.setUser(user);
            newWallet.setBalance(0.0);
            newWallet.setCurrency("TND");
            return walletRepository.save(newWallet);
        });
    }

    private WalletResponse mapToResponse(Wallet wallet) {
        // 1. Card transactions (deposit/withdraw via card)
        List<TransactionCard> cardTxs = transactionCardRepository.findByWalletIdOrderByTimestampDesc(wallet.getId());
        List<TransactionResponse> walletTx = cardTxs.stream().map(tx -> new TransactionResponse(
                tx.getId(),
                tx.getAmount(),
                tx.getType().name(),
                tx.getStatus().name(),
                tx.getDescription(),
                tx.getTimestamp()
        )).collect(Collectors.toList());

        // 2. Payment transactions (online trip payments linked to wallet)
        List<TransactionPaiement> paiementTxs = transactionPaiementRepository.findByWalletIdOrderByTimestampDesc(wallet.getId());
        List<TransactionResponse> paiementTx = paiementTxs.stream().map(tx -> new TransactionResponse(
                tx.getId(),
                tx.getAmount(),
                tx.getType(),
                tx.getStatus(),
                tx.getDescription(),
                tx.getTimestamp()
        )).collect(Collectors.toList());

        // 3. Cash trip payments (synthesized from trajets — no wallet deduction)
        User user = wallet.getUser();
        List<com.example.taximotoapp_backend.trajet.model.Trajet> userTrajets;
        boolean isClient = false;

        if (user instanceof com.example.taximotoapp_backend.User.model.Client client) {
            userTrajets = client.getTrajets();
            isClient = true;
        } else if (user instanceof com.example.taximotoapp_backend.User.model.Chauffeur chauffeur) {
            userTrajets = chauffeur.getTrajets();
        } else {
            userTrajets = java.util.Collections.emptyList();
        }

        final boolean finalIsClient = isClient;
        // Cash synthesis only for CLIENT (driver cash earnings are recorded as TransactionPaiement)
        List<TransactionResponse> cashTx = finalIsClient
                ? userTrajets.stream()
                .filter(t -> t.getPaiement() != null && t.getPaiement().getType() == com.example.taximotoapp_backend.model.enumClass.PaiementType.CASH)
                .map(t -> {
                    String dest = t.getTrajetLocation() != null ? t.getTrajetLocation().getDestinationAddress() : null;
                    String label = (finalIsClient ? "Paiement espèces" : "Gain espèces") + " → " + (dest != null && !dest.isEmpty() ? dest : "Trajet #" + t.getId());
                    return new TransactionResponse(
                            t.getPaiement().getId(),
                            t.getPaiement().getMontant(),
                            finalIsClient ? "PAYMENT" : "EARNING",
                            t.getPaiement().getStatus().name(),
                            label,
                            t.getPaiement().getDatePaiement() != null ? t.getPaiement().getDatePaiement() : t.getRequestedAt()
                    );
                })
                .collect(Collectors.toList())
                : java.util.Collections.emptyList();

        List<TransactionResponse> allTx = new java.util.ArrayList<>();
        allTx.addAll(walletTx);
        allTx.addAll(paiementTx);
        allTx.addAll(cashTx);

        // Sort descending by timestamp
        allTx.sort((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp()));

        // Limit to 5
        List<TransactionResponse> recentTx = allTx.stream().limit(5).collect(Collectors.toList());

        return new WalletResponse(
                wallet.getId(),
                wallet.getBalance(),
                wallet.getCashBalance(),
                wallet.getCurrency(),
                recentTx
        );
    }
}

