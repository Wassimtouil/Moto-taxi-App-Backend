package com.example.taximotoapp_backend.paiement.service;

import com.example.taximotoapp_backend.Admin.dto.AdminPaiementDto;
import com.example.taximotoapp_backend.Admin.dto.AdminPaiementStatsDto;
import com.example.taximotoapp_backend.Admin.dto.AdminTransactionDto;
import com.example.taximotoapp_backend.Admin.dto.AdminWalletDto;
import com.example.taximotoapp_backend.model.enumClass.PaiementStatus;
import com.example.taximotoapp_backend.model.enumClass.PaiementType;
import com.example.taximotoapp_backend.paiement.dto.request.PaiementRequest;
import com.example.taximotoapp_backend.paiement.dto.response.PaiementResponse;
import com.example.taximotoapp_backend.paiement.mapper.PaiementMapper;
import com.example.taximotoapp_backend.paiement.model.Paiement;
import com.example.taximotoapp_backend.paiement.model.Transaction;
import com.example.taximotoapp_backend.paiement.repository.PaiementRepository;
import com.example.taximotoapp_backend.paiement.repository.TransactionRepository;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaiementService {
    private final PaiementRepository paiementRepository;
    private final TrajetRepository trajetRepository;
    private final TransactionRepository transactionRepository;
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

    public AdminPaiementStatsDto getPaiementStats(){
        List<Paiement> paiements = paiementRepository.findAll();
        List<Transaction> transactions = transactionRepository.findAll();

        long totalPaiements = paiements.size();
        long totalTransactions = transactions.size();

        // Exact driver revenue based on completed payments (PAYE)
        Double totalDriverRevenue = paiements.stream()
                .filter(p -> p.getStatus() != null && p.getStatus().name().equals("PAYE"))
                .mapToDouble(p -> p.getMontant() != null ? p.getMontant() : 0.0)
                .sum();

        AdminPaiementStatsDto stats = new AdminPaiementStatsDto(
                totalTransactions,
                totalPaiements,
                totalDriverRevenue
        );
        return stats;
    }
    public List<AdminPaiementDto> getAllPaiements(){
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
                p.getTrajet() != null && p.getTrajet().getChauffeur() != null ? p.getTrajet().getChauffeur().getFullName() : null,
                p.getTrajet() != null && p.getTrajet().getClient() != null ? p.getTrajet().getClient().getPhotoBase64() : null,
                p.getTrajet() != null && p.getTrajet().getChauffeur() != null ? p.getTrajet().getChauffeur().getPhotoBase64() : null
        )).collect(Collectors.toList());
        return dtos;
    }
    public List<AdminTransactionDto> getAllTransactions(){
        List<AdminTransactionDto> dtos = transactionRepository.findAll().stream().map(t -> {
            String userName = "Unknown";
            String userRole = "Unknown";
            String userPhotoBase64 = null;
            Long userId = null;
            if (t.getWallet() != null && t.getWallet().getUser() != null) {
                userName = t.getWallet().getUser().getFullName();
                userRole = t.getWallet().getUser().getRole() != null ? t.getWallet().getUser().getRole().name() : "Unknown";
                userId = t.getWallet().getUser().getId();
                userPhotoBase64 = t.getWallet().getUser().getPhotoBase64();
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
                    userRole,
                    userPhotoBase64
            );}).collect(Collectors.toList());
        return dtos;
    }
    public List<AdminWalletDto> getAllWallets() {
        List<AdminWalletDto> dtos = walletRepository.findAll().stream().map(w -> {
            String userName = "Unknown";
            String userEmail = "Unknown";
            String userRole = "Unknown";
            String userPhotoBase64 = null;
            Long userId = null;
            if (w.getUser() != null) {
                userName = w.getUser().getFullName();
                userEmail = w.getUser().getEmail();
                userRole = w.getUser().getRole() != null ? w.getUser().getRole().name() : "Unknown";
                userId = w.getUser().getId();
                userPhotoBase64 = w.getUser().getPhotoBase64();
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
                    userPhotoBase64,
                    w.getCreatedAt(),
                    w.getUpdatedAt()
            );
        }).collect(Collectors.toList());
        return dtos;
    }
}
