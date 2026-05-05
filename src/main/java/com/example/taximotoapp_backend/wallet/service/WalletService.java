package com.example.taximotoapp_backend.wallet.service;

import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.wallet.dto.request.DepositRequest;
import com.example.taximotoapp_backend.wallet.dto.request.WithdrawRequest;
import com.example.taximotoapp_backend.wallet.dto.response.ApiResponse;
import com.example.taximotoapp_backend.wallet.dto.response.TransactionResponse;
import com.example.taximotoapp_backend.wallet.dto.response.WalletResponse;
import com.example.taximotoapp_backend.wallet.model.PaymentCard;
import com.example.taximotoapp_backend.wallet.model.Transaction;
import com.example.taximotoapp_backend.wallet.model.Wallet;
import com.example.taximotoapp_backend.model.enumClass.TransactionStatus;
import com.example.taximotoapp_backend.model.enumClass.TransactionType;
import com.example.taximotoapp_backend.wallet.repository.PaymentCardRepository;
import com.example.taximotoapp_backend.wallet.repository.TransactionRepository;
import com.example.taximotoapp_backend.wallet.repository.WalletRepository;
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
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final PaymentCardRepository cardRepository;

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

        PaymentCard card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (!card.getUser().getId().equals(userId)) {
            throw new RuntimeException("Card does not belong to user");
        }

        Wallet wallet = getOrCreateWallet(userId);
        wallet.setBalance(wallet.getBalance() + request.getAmount());
        walletRepository.save(wallet);

        createTransaction(wallet, request.getAmount(), TransactionType.DEPOSIT, TransactionStatus.COMPLETED, "Deposit via card " + card.getLast4Digits());
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

        PaymentCard card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (!card.getUser().getId().equals(userId)) {
            throw new RuntimeException("Card does not belong to user");
        }

        wallet.setBalance(wallet.getBalance() - request.getAmount());
        walletRepository.save(wallet);

        createTransaction(wallet, request.getAmount(), TransactionType.WITHDRAWAL, TransactionStatus.COMPLETED, "Withdrawal to card " + card.getLast4Digits());
        return new ApiResponse(true, "Withdrawal successful");
    }

    @Transactional
    public Transaction createTransaction(Wallet wallet, Double amount, TransactionType type, TransactionStatus status, String description) {
        Transaction tx = new Transaction();
        tx.setWallet(wallet);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setStatus(status);
        tx.setDescription(description);
        return transactionRepository.save(tx);
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
        List<Transaction> txs = transactionRepository.findByWalletIdOrderByTimestampDesc(wallet.getId());
        List<TransactionResponse> txResponses = txs.stream().map(tx -> new TransactionResponse(
                tx.getId(),
                tx.getAmount(),
                tx.getType().name(),
                tx.getStatus().name(),
                tx.getDescription(),
                tx.getTimestamp()
        )).collect(Collectors.toList());

        return new WalletResponse(
                wallet.getId(),
                wallet.getBalance(),
                wallet.getCashBalance(),
                wallet.getCurrency(),
                txResponses
        );
    }
}
