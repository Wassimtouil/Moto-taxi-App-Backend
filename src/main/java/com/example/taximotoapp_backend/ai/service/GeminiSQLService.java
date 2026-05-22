package com.example.taximotoapp_backend.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiSQLService {

    private final WebClient webClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    public String generateSQL(String question) {

        String prompt = """
        You are a senior SQL expert.

        RULES:
        - Only SELECT queries
        - MySQL only
        - Must use LIMIT 10
        - No explanation
        - No markdown

        DATABASE:
        drivers(id, name, rating)
        trips(id, driver_id, revenue, status)

        QUESTION:
        """ + question;

        return callGemini(prompt);
    }

    public String simpleText(String prompt) {
        return callGemini(prompt);
    }

    private String callGemini(String prompt) {

        String url =
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key="
                        + apiKey;

        Map<String, Object> body = Map.of(
                "contents",
                List.of(
                        Map.of(
                                "parts",
                                List.of(Map.of("text", prompt))
                        )
                )
        );

        return webClient.post()
                .uri(url)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}