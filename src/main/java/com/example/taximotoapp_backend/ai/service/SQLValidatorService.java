package com.example.taximotoapp_backend.ai.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SQLValidatorService {

    public void validate(String sql) {
        String q = sql.toLowerCase();

        // Only SELECT allowed
        if (!q.startsWith("select")) {
            throw new RuntimeException("Only SELECT allowed");
        }
        // Block dangerous keywords
        List<String> forbidden = List.of(
                "delete",
                "update",
                "insert",
                "drop",
                "alter",
                "truncate"
        );

        for (String f : forbidden) {
            if (q.contains(f)) {
                throw new RuntimeException("Forbidden SQL detected");
            }
        }

        // enforce safety limit
        if (!q.contains("limit")) {
            throw new RuntimeException("Missing LIMIT for safety");
        }
    }
}