package com.example.taximotoapp_backend.wallet.service;

import com.example.taximotoapp_backend.User.model.Chauffeur;
import com.example.taximotoapp_backend.User.model.Client;
import com.example.taximotoapp_backend.model.enumClass.PaiementStatus;
import com.example.taximotoapp_backend.model.enumClass.PaiementType;
import com.example.taximotoapp_backend.paiement.model.Paiement;
import com.example.taximotoapp_backend.paiement.repository.PaiementRepository;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import com.example.taximotoapp_backend.trajet.repository.TrajetRepository;
import com.example.taximotoapp_backend.wallet.dto.response.ApiResponse;
import com.example.taximotoapp_backend.wallet.model.Wallet;
import com.example.taximotoapp_backend.model.enumClass.TransactionStatus;
import com.example.taximotoapp_backend.model.enumClass.TransactionType;
import com.example.taximotoapp_backend.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final WalletService walletService;
    private final WalletRepository walletRepository;
    private final TrajetRepository trajetRepository;
    private final PaiementRepository paiementRepository;

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
                walletService.createTransaction(clientWallet, price, TransactionType.PAYMENT, TransactionStatus.COMPLETED, "Payment for trip " + tripId);

                // Add to driver
                if (driver != null) {
                    Wallet driverWallet = walletService.getOrCreateWallet(driver.getId());
                    driverWallet.setBalance(driverWallet.getBalance() + price);
                    walletRepository.save(driverWallet);
                    walletService.createTransaction(driverWallet, price, TransactionType.DEPOSIT, TransactionStatus.COMPLETED, "Earnings for trip " + tripId);
                }
            } else if (paiement.getType() == PaiementType.CASH) {
                // For cash payments, just update the driver's cash tracking balance
                if (driver != null) {
                    Wallet driverWallet = walletService.getOrCreateWallet(driver.getId());
                    Double currentCash = driverWallet.getCashBalance() != null ? driverWallet.getCashBalance() : 0.0;
                    driverWallet.setCashBalance(currentCash + price);
                    walletRepository.save(driverWallet);
                    // Optionally record a transaction for the cash collection, but maybe skip it to keep transaction history focused on app-managed funds
                    walletService.createTransaction(driverWallet, price, TransactionType.DEPOSIT, TransactionStatus.COMPLETED, "Cash collected for trip " + tripId);
                }
            }

            paiement.setStatus(PaiementStatus.PAYE);
            paiementRepository.save(paiement);
        }

        return new ApiResponse(true, "Payment processed successfully");
    }
}
