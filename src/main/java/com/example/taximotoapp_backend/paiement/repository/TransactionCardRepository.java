package com.example.taximotoapp_backend.paiement.repository;

import com.example.taximotoapp_backend.paiement.model.TransactionCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionCardRepository extends JpaRepository<TransactionCard, Long> {
    List<TransactionCard> findByWalletIdOrderByTimestampDesc(Long walletId);
}
