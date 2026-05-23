package com.example.taximotoapp_backend.ai.service;

import com.example.taximotoapp_backend.ai.dto.SaveQueryRequest;
import com.example.taximotoapp_backend.ai.model.AICachedQuery;
import com.example.taximotoapp_backend.ai.repository.AICachedQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CachedQueryService {

    private final AICachedQueryRepository cachedQueryRepository;
    private final LanguageNormalizerService normalizer;

    public Map<String, Object> saveQuery(SaveQueryRequest request) {
        String normalizedQuestion = normalizer.normalize(request.getQuestion()).toLowerCase().trim();
        if (cachedQueryRepository.findByQuestion(normalizedQuestion).isPresent()) {
            return Map.of("message", "Cette requête est déjà en cache.");
        }
        AICachedQuery cached = new AICachedQuery();
        cached.setQuestion(normalizedQuestion);
        cached.setSqlQuery(request.getSql());
        cached.setCreatedAt(LocalDateTime.now());
        cachedQueryRepository.save(cached);
        return Map.of("message", "Requête sauvegardée avec succès !");
    }

    public List<Map<String, Object>> getCachedQueries() {
        return cachedQueryRepository.findAll()
                .stream()
                .map(q -> Map.<String, Object>of(
                        "id", q.getId(),
                        "question", q.getQuestion(),
                        "createdAt", q.getCreatedAt().toString()
                ))
                .collect(Collectors.toList());
    }
}