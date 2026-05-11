package com.example.taximotoapp_backend.paiement.controller;

import com.example.taximotoapp_backend.paiement.dto.response.*;
import com.example.taximotoapp_backend.paiement.model.Paiement;
import com.example.taximotoapp_backend.paiement.model.Transaction;
import com.example.taximotoapp_backend.paiement.model.Wallet;
import com.example.taximotoapp_backend.paiement.repository.PaiementRepository;
import com.example.taximotoapp_backend.paiement.repository.TransactionRepository;
import com.example.taximotoapp_backend.paiement.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/paiements")
@RequiredArgsConstructor
public class AdminPaiementController {

    private final PaiementRepository paiementRepository;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    @GetMapping("/stats")
    public ResponseEntity<AdminPaiementStatsDto> getPaiementStats() {
        List<Paiement> paiements = paiementRepository.findAll();
        List<Transaction> transactions = transactionRepository.findAll();

        Double totalRevenue = paiements.stream()
                .mapToDouble(p -> p.getMontant() != null ? p.getMontant() : 0.0)
                .sum();

        long totalPaiements = paiements.size();
        long totalTransactions = transactions.size();

        // Exact driver revenue based on completed payments (PAYE)
        Double totalDriverRevenue = paiements.stream()
                .filter(p -> p.getStatus() != null && p.getStatus().name().equals("PAYE"))
                .mapToDouble(p -> p.getMontant() != null ? p.getMontant() : 0.0)
                .sum();

        AdminPaiementStatsDto stats = new AdminPaiementStatsDto(
                totalRevenue,
                totalTransactions,
                totalPaiements,
                totalDriverRevenue
        );

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/all")
    public ResponseEntity<List<AdminPaiementDto>> getAllPaiements() {
        List<AdminPaiementDto> dtos = paiementRepository.findAll().stream().map(p -> new AdminPaiementDto(
                p.getId(),
                p.getMontant(),
                p.getType() != null ? p.getType().name() : null,
                p.getStatus() != null ? p.getStatus().name() : null,
                p.getDatePaiement(),
                p.getTrajet() != null ? p.getTrajet().getId() : null,
                p.getTrajet() != null && p.getTrajet().getClient() != null ? p.getTrajet().getClient().getId() : null,
                p.getTrajet() != null && p.getTrajet().getClient() != null ? p.getTrajet().getClient().getFullName() : null,
                p.getTrajet() != null && p.getTrajet().getChauffeur() != null ? p.getTrajet().getChauffeur().getId() : null,
                p.getTrajet() != null && p.getTrajet().getChauffeur() != null ? p.getTrajet().getChauffeur().getFullName() : null
        )).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<AdminTransactionDto>> getAllTransactions() {
        List<AdminTransactionDto> dtos = transactionRepository.findAll().stream().map(t -> {
            String userName = "Unknown";
            String userRole = "Unknown";
            Long userId = null;
            if (t.getWallet() != null && t.getWallet().getUser() != null) {
                userName = t.getWallet().getUser().getFullName();
                userRole = t.getWallet().getUser().getRole() != null ? t.getWallet().getUser().getRole().name() : "Unknown";
                userId = t.getWallet().getUser().getId();
            }

            return new AdminTransactionDto(
                    t.getId(),
                    t.getAmount(),
                    t.getType() != null ? t.getType().name() : null,
                    t.getStatus() != null ? t.getStatus().name() : null,
                    t.getDescription(),
                    t.getTimestamp(),
                    t.getWallet() != null ? t.getWallet().getId() : null,
                    userId,
                    userName,
                    userRole
            );}).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/wallets")
    public ResponseEntity<List<AdminWalletDto>> getAllWallets() {
        List<AdminWalletDto> dtos = walletRepository.findAll().stream().map(w -> {
            String userName = "Unknown";
            String userEmail = "Unknown";
            String userRole = "Unknown";
            Long userId = null;
            if (w.getUser() != null) {
                userName = w.getUser().getFullName();
                userEmail = w.getUser().getEmail();
                userRole = w.getUser().getRole() != null ? w.getUser().getRole().name() : "Unknown";
                userId = w.getUser().getId();
            }

            return new AdminWalletDto(
                    w.getId(),
                    w.getBalance(),
                    w.getCashBalance(),
                    w.getCurrency(),
                    userId,
                    userName,
                    userEmail,
                    userRole,
                    w.getCreatedAt(),
                    w.getUpdatedAt()
            );}).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
