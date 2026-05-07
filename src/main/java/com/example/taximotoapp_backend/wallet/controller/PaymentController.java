package com.example.taximotoapp_backend.wallet.controller;

import com.example.taximotoapp_backend.wallet.dto.response.ApiResponse;
import com.example.taximotoapp_backend.wallet.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/trip/{tripId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'CHAUFFEUR')")
    public ResponseEntity<ApiResponse> processTripPayment(@PathVariable Long tripId) {
        try {
            return ResponseEntity.ok(paymentService.processTripPayment(tripId));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Insufficient funds")) {
                return ResponseEntity.status(402).body(new ApiResponse(false, "Insufficient funds"));
            }
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
}
