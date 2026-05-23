package com.example.taximotoapp_backend.ai.controller;

import com.example.taximotoapp_backend.ai.dto.ChatRequest;
import com.example.taximotoapp_backend.ai.dto.SaveQueryRequest;
import com.example.taximotoapp_backend.ai.model.AICachedQuery;
import com.example.taximotoapp_backend.ai.repository.AICachedQueryRepository;
import com.example.taximotoapp_backend.ai.service.AIOrchestratorService;
import com.example.taximotoapp_backend.ai.dto.ChatResponse;
import com.example.taximotoapp_backend.ai.service.CachedQueryService;
import com.example.taximotoapp_backend.ai.service.LanguageNormalizerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ia")
@RequiredArgsConstructor
public class AIChatController {

    private final AIOrchestratorService service;
    private final AICachedQueryRepository cachedQueryRepository;
    private final CachedQueryService cachedQueryService;

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        System.out.println("aaaaa");
        return service.handle(request.getMessage());
    }


    @PostMapping("/save-query")
    public ResponseEntity<?> saveQuery(@RequestBody SaveQueryRequest request){
        return ResponseEntity.ok(cachedQueryService.saveQuery(request));
    }


    @GetMapping("/cached-queries")
    public ResponseEntity<?> getCachedQueries() {
        return ResponseEntity.ok(cachedQueryService.getCachedQueries());
    }
}