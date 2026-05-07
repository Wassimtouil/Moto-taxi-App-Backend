package com.example.taximotoapp_backend.paiement.repository;

import com.example.taximotoapp_backend.paiement.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUser(com.example.taximotoapp_backend.User.model.User user);
}

