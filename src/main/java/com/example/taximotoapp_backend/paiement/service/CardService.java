package com.example.taximotoapp_backend.paiement.service;

import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.paiement.dto.request.AddCardRequest;
import com.example.taximotoapp_backend.paiement.dto.response.ApiResponse;
import com.example.taximotoapp_backend.paiement.dto.response.CardResponse;
import com.example.taximotoapp_backend.paiement.model.Card;
import com.example.taximotoapp_backend.paiement.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public List<CardResponse> getUserCards(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return cardRepository.findByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ApiResponse addCard(Long userId, AddCardRequest request) {
        if (!validateCard(request.getCardNumber(), request.getExpiryMonth(), request.getExpiryYear(),
                request.getCvv())) {
            throw new IllegalArgumentException("Invalid card details");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String cleanNumber = request.getCardNumber().replaceAll("\\s+", "");
        String last4 = cleanNumber.substring(cleanNumber.length() - 4);
        String brand = request.getBrand();
        if (brand == null || brand.isEmpty()) {
            brand = determineBrand(cleanNumber);
        }

        Card card = new Card();
        card.setCardHolderName(request.getCardHolderName());
        card.setLast4Digits(last4);
        card.setBrand(brand);
        card.setExpiryMonth(request.getExpiryMonth());
        card.setExpiryYear(request.getExpiryYear());
        card.setUser(user);

        List<Card> existingCards = cardRepository.findByUser(user);
        if (existingCards.isEmpty() || (request.getIsDefault() != null && request.getIsDefault())) {
            if (request.getIsDefault() != null && request.getIsDefault()) {
                existingCards.forEach(c -> {
                    c.setIsDefault(false);
                    cardRepository.save(c);
                });
            }
            card.setIsDefault(true);
        } else {
            card.setIsDefault(false);
        }

        cardRepository.save(card);
        return new ApiResponse(true, "Card added successfully");
    }

    @Transactional
    public ApiResponse deleteCard(Long userId, Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        if (!card.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this card");
        }
        cardRepository.delete(card);
        return new ApiResponse(true, "Card deleted successfully");
    }

    @Transactional
    public ApiResponse setDefaultCard(Long userId, Long cardId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Card> cards = cardRepository.findByUser(user);
        boolean found = false;
        for (Card c : cards) {
            if (c.getId().equals(cardId)) {
                c.setIsDefault(true);
                found = true;
            } else {
                c.setIsDefault(false);
            }
            cardRepository.save(c);
        }
        if (!found)
            throw new RuntimeException("Card not found");
        return new ApiResponse(true, "Default card updated");
    }

    private boolean validateCard(String cardNumber, String month, String year, String cvv) {
        if (cardNumber == null || month == null || year == null || cvv == null)
            return false;
        String cleanNumber = cardNumber.replaceAll("\\s+", "");
        return cleanNumber.length() >= 13 && cvv.length() >= 3;
    }

    private boolean validateLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    private String determineBrand(String cardNumber) {
        if (cardNumber.startsWith("4"))
            return "Visa";
        if (cardNumber.matches("^5[1-5].*"))
            return "MasterCard";
        if (cardNumber.matches("^3[47].*"))
            return "Amex";
        return "Unknown";
    }

    private CardResponse mapToResponse(Card card) {
        return new CardResponse(
                card.getId(),
                card.getCardHolderName(),
                card.getLast4Digits(),
                card.getBrand(),
                card.getExpiryMonth(),
                card.getExpiryYear(),
                card.getIsDefault(),
                card.getCreatedAt());
    }
}

