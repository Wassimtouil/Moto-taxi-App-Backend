package com.example.taximotoapp_backend.paiement.repository;

import com.example.taximotoapp_backend.paiement.model.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {
    List<PaymentCard> findByUser(com.example.taximotoapp_backend.User.model.User user);
}

