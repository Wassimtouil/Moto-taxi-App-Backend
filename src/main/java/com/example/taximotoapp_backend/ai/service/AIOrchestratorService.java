package com.example.taximotoapp_backend.ai.service;

import com.example.taximotoapp_backend.ai.util.SQLExtractor;
import com.example.taximotoapp_backend.chat.dto.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIOrchestratorService {

    private final LanguageNormalizerService normalizer;
    private final GeminiSQLService geminiSQLService;
    private final SQLValidatorService validator;
    private final SQLExecutorService executor;
    private final ResponseFormatterService formatter;

    public ChatResponse handle(String userMessage) {

        // 1. Normalize language (Tunisian/French/English)
        String normalized = normalizer.normalize(userMessage);

        // 2. Generate SQL via AI
        String rawSQL = geminiSQLService.generateSQL(normalized);

        // 3. Clean SQL
        String sql = SQLExtractor.extract(rawSQL);

        // 4. Security validation
        validator.validate(sql);

        // 5. Execute SQL
        Object data = executor.execute(sql);

        // 6. Build natural response
        String response = formatter.format(normalized, data);

        return new ChatResponse(response, sql, data);
    }
}