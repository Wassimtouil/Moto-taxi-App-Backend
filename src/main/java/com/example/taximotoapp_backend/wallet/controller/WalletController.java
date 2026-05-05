package com.example.taximotoapp_backend.wallet.controller;

import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.wallet.dto.request.DepositRequest;
import com.example.taximotoapp_backend.wallet.dto.request.WithdrawRequest;
import com.example.taximotoapp_backend.wallet.dto.response.ApiResponse;
import com.example.taximotoapp_backend.wallet.dto.response.WalletResponse;
import com.example.taximotoapp_backend.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;
    private final UserRepository userRepository;

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable Long userId) {
        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(walletService.getWalletByUserId(userId));
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse> deposit(@RequestBody DepositRequest request) {
        return ResponseEntity.ok(walletService.deposit(getCurrentUserId(), request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse> withdraw(@RequestBody WithdrawRequest request) {
        return ResponseEntity.ok(walletService.withdraw(getCurrentUserId(), request));
    }
}
