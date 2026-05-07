package com.example.taximotoapp_backend.wallet.controller;

import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.wallet.dto.request.AddCardRequest;
import com.example.taximotoapp_backend.wallet.dto.response.ApiResponse;
import com.example.taximotoapp_backend.wallet.dto.response.CardResponse;
import com.example.taximotoapp_backend.wallet.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;
    private final UserRepository userRepository;

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<CardResponse>> getUserCards(@PathVariable Long userId) {
        if (!getCurrentUserId().equals(userId)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(cardService.getUserCards(userId));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> addCard(@RequestBody AddCardRequest request) {
        return ResponseEntity.ok(cardService.addCard(getCurrentUserId(), request));
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<ApiResponse> deleteCard(@PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.deleteCard(getCurrentUserId(), cardId));
    }

    @PutMapping("/{cardId}/default")
    public ResponseEntity<ApiResponse> setDefaultCard(@PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.setDefaultCard(getCurrentUserId(), cardId));
    }
}
