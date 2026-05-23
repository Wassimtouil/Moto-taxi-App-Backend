package com.example.taximotoapp_backend.ai.service;

import com.example.taximotoapp_backend.ai.model.AICachedQuery;
import com.example.taximotoapp_backend.ai.repository.AICachedQueryRepository;
import com.example.taximotoapp_backend.ai.util.SQLExtractor;
import com.example.taximotoapp_backend.ai.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AIOrchestratorService {

    private final LanguageNormalizerService normalizer;
    private final GrokSQLService grokSQLService;
    private final SQLValidatorService validator;
    private final SQLExecutorService executor;
    private final ResponseFormatterService formatter;
    private final AICachedQueryRepository cachedQueryRepository;

    public ChatResponse handle(String userMessage) {

        // 1. Normalize language (Tunisian/French/English)
        String normalized = normalizer.normalize(userMessage);

        // 2. Check cache first
        Optional<AICachedQuery> cached = cachedQueryRepository.findByQuestion(normalized.toLowerCase().trim());
        if (cached.isPresent()) {
            String cachedSql = cached.get().getSqlQuery();
            validator.validate(cachedSql);
            Object data = executor.execute(cachedSql);
            String response = formatter.format(normalized, data);
            return new ChatResponse(response, cachedSql, data, true);
        }

        System.out.println("avec ia iciiiiiiiiiiiiiiii");
        // 3. Generate SQL via AI (no cache hit)
        String rawSQL = grokSQLService.generateSQL(normalized);

        // 4. Clean SQL
        String sql = SQLExtractor.extract(rawSQL);

        // 5. Security validation
        validator.validate(sql);

        // 6. Execute SQL
        Object data = executor.execute(sql);

        // 7. Build natural response
        String response = formatter.format(normalized, data);

        return new ChatResponse(response, sql, data, false);
    }
}