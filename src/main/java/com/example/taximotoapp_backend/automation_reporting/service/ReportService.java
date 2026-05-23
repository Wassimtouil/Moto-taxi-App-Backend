package com.example.taximotoapp_backend.automation_reporting.service;

import com.example.taximotoapp_backend.automation_reporting.dto.DailyReportDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final DashboardStatisticsService statisticsService;
    private final PdfReportService pdfReportService;
    private final EmailService emailService;
    private final AIAnalysisService aiAnalysisService;

    public void generateAndSendReport() {

        List<Map<String, Object>> reclamations =
                statisticsService.getTodayReclamations();

        String aiAnalysis =
                aiAnalysisService.analyzeReclamations(reclamations);

        DailyReportDTO dto = DailyReportDTO.builder()
                .todayRevenue(statisticsService.getTodayRevenue())
                .totalTrips(statisticsService.getTodayTrips())
                .inactiveDrivers(statisticsService.getInactiveDrivers())
                .newClients(statisticsService.getNewClients())
                .cancellationRate(statisticsService.getCancellationRate())
                .paymentStats(statisticsService.getPaymentStats())
                .topDrivers(statisticsService.getTopDrivers())
                .reclamations(reclamations)
                .aiAnalysis(aiAnalysis)
                .build();

        byte[] pdf = pdfReportService.generateDailyReport(dto);

        emailService.sendReport(pdf);
    }
}
