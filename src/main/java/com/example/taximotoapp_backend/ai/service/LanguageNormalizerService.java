package com.example.taximotoapp_backend.ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LanguageNormalizerService {
    private final GeminiSQLService geminiSQLService;
    public String normalize(String message) {
        String prompt =
                "Convert this text into clear French business question.\n" +
                        "User message: " + message + "\n" +
                        "Return only the cleaned sentence.";

        return geminiSQLService.simpleText(prompt);
    }
}