package com.example.taximotoapp_backend.Admin.controller;

import com.example.taximotoapp_backend.Admin.dto.AdminPaiementDto;
import com.example.taximotoapp_backend.Admin.dto.AdminPaiementStatsDto;
import com.example.taximotoapp_backend.Admin.dto.AdminTransactionDto;
import com.example.taximotoapp_backend.Admin.dto.AdminWalletDto;
import com.example.taximotoapp_backend.paiement.service.PaiementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/paiements")
@RequiredArgsConstructor
public class AdminPaiementController {
    private final PaiementService paiementService;

    @GetMapping("/stats")
    public ResponseEntity<AdminPaiementStatsDto> getPaiementStats() {
        return ResponseEntity.ok(paiementService.getPaiementStats());
    }
    @GetMapping("/all")
    public ResponseEntity<List<AdminPaiementDto>> getAllPaiements() {
        return ResponseEntity.ok(paiementService.getAllPaiements());
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<AdminTransactionDto>> getAllTransactions() {
        return ResponseEntity.ok(paiementService.getAllTransactions());
    }

    @GetMapping("/wallets")
    public ResponseEntity<List<AdminWalletDto>> getAllWallets() {
        return ResponseEntity.ok(paiementService.getAllWallets());
    }
}
