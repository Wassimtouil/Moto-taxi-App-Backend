package com.example.taximotoapp_backend.automation_reporting.service;

import com.example.taximotoapp_backend.automation_reporting.dto.DailyReportDTO;
import com.example.taximotoapp_backend.automation_reporting.dto.TopDriverDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final DashboardStatisticsService statisticsService;
    private final PdfReportService pdfReportService;
    private final EmailService emailService;
    private final AIAnalysisService aiAnalysisService;
    private SpringTemplateEngine templateEngine;

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

        // 1. Convert DTO → HTML
        Context context = new Context();

        context.setVariable("date", LocalDate.now());
        context.setVariable("revenue", dto.getTodayRevenue());
        context.setVariable("trips", dto.getTotalTrips());
        context.setVariable("drivers", dto.getInactiveDrivers());

        context.setVariable("drivers_rows", buildDriversRows(dto.getTopDrivers()));
        context.setVariable("risk_rows", buildRiskRows(dto.getReclamations()));
        context.setVariable("recommendations", dto.getAiAnalysis());

        String html = templateEngine.process("report", context);

        // 2. HTML → PDF
        byte[] pdf = pdfReportService.generateFromHtml(html);

        // 3. Email
        emailService.sendReport(pdf);
    }
    private String buildDriversRows(List<TopDriverDTO> drivers) {

        StringBuilder sb = new StringBuilder();

        for (TopDriverDTO d : drivers) {
            sb.append("<tr>")
                    .append("<td>").append(d.getDriverName()).append("</td>")
                    .append("<td>").append(d.getRevenue()).append("</td>")
                    .append("<td>").append(d.getTrips()).append("</td>")
                    .append("</tr>");
        }

        return sb.toString();
    }
    private String buildRiskRows(List<Map<String, Object>> recs) {

        StringBuilder sb = new StringBuilder();

        for (Map<String, Object> r : recs) {

            sb.append("<tr>")
                    .append("<td>").append(r.get("objet")).append("</td>")
                    .append("<td><span class='risk-medium'>MEDIUM</span></td>")
                    .append("<td>").append(r.get("message")).append("</td>")
                    .append("</tr>");
        }

        return sb.toString();
    }
}
