package com.example.taximotoapp_backend.automation_reporting.controller;

import com.example.taximotoapp_backend.automation_reporting.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class Test {
    private final ReportService reportService;


    @PostMapping("/generate")
    public ResponseEntity<?> generateReportNow() {
        try {
            reportService.generateAndSendReport();
            return ResponseEntity.ok("Report sent");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
