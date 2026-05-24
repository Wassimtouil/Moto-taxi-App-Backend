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

    private final SpringTemplateEngine templateEngine;
    private final PdfReportService pdfReportService;
    private final EmailService emailService;
    private final DashboardStatisticsService statisticsService;

    public void generateAndSendReport() {

        var reclamations = statisticsService.getTodayReclamations();

        DailyReportDTO dto = DailyReportDTO.builder()
                .todayRevenue(statisticsService.getTodayRevenue())
                .totalTrips(statisticsService.getTodayTrips())
                .inactiveDrivers(statisticsService.getInactiveDrivers())
                .topDrivers(statisticsService.getTopDrivers())
                .build();

        Context context = new Context();
        context.setVariable("date", LocalDate.now().toString());
        context.setVariable("revenue", dto.getTodayRevenue());
        context.setVariable("trips", dto.getTotalTrips());
        context.setVariable("drivers", dto.getInactiveDrivers());
        context.setVariable("topDrivers", dto.getTopDrivers());

        String html = templateEngine.process("report", context);

        byte[] pdf = pdfReportService.generateFromHtml(html);

        emailService.sendReport(pdf);
    }
}