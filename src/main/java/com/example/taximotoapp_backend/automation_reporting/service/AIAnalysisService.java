package com.example.taximotoapp_backend.automation_reporting.service;

import com.example.taximotoapp_backend.ai.service.GrokSQLService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIAnalysisService {

    private final GrokSQLService grokSQLService;

    public String analyzeReclamations(List<Map<String, Object>> reclamations) {

        String prompt = """
                Analyze these taxi platform reclamations.

                Return:
                - Global sentiment
                - Main problems
                - Risk analysis
                - Recommendations

                Reclamations:
                """ + reclamations.toString();

        return grokSQLService.simpleText(prompt);
    }
}