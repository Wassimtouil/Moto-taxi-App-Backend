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

        // ── Collecte de toutes les données ──────────────────────────
        DailyReportDTO dto = DailyReportDTO.builder()

                // KPIs principaux
                .todayRevenue(statisticsService.getTodayRevenue())
                .totalTrips(statisticsService.getTodayTrips())
                .completedTrips(statisticsService.getCompletedTrips())
                .canceledTrips(statisticsService.getCanceledTrips())
                .cancellationRate(statisticsService.getCancellationRate())

                // Chauffeurs & clients
                .activeDrivers(statisticsService.getActiveDrivers())
                .inactiveDrivers(statisticsService.getInactiveDrivers())
                .activeClients(statisticsService.getActiveClients())
                .newClients(statisticsService.getNewClients())

                // Finance
                .paymentStats(statisticsService.getPaymentStats())
                .cashRevenue(statisticsService.getCashRevenue())
                .onlineRevenue(statisticsService.getOnlineRevenue())

                .build();

        // ── Construction du contexte Thymeleaf ──────────────────────
        Context context = new Context();
        context.setVariable("date",LocalDate.now().toString());

        // KPIs
        context.setVariable("revenue",           dto.getTodayRevenue());
        context.setVariable("trips",             dto.getTotalTrips());
        context.setVariable("completedTrips",    dto.getCompletedTrips());
        context.setVariable("canceledTrips",     dto.getCanceledTrips());
        context.setVariable("cancellationRate",  dto.getCancellationRate());

        // Chauffeurs & clients
        context.setVariable("activeDrivers",     dto.getActiveDrivers());
        context.setVariable("inactiveDrivers",   dto.getInactiveDrivers());
        context.setVariable("activeClients",     dto.getActiveClients());
        context.setVariable("newClients",        dto.getNewClients());


        // Finance
        context.setVariable("paymentStats",      dto.getPaymentStats());
        context.setVariable("cashRevenue",       dto.getCashRevenue());
        context.setVariable("onlineRevenue",     dto.getOnlineRevenue());

        // ── Génération HTML → PDF → Email ───────────────────────────
        String html = templateEngine.process("report", context);
        byte[] pdf  = pdfReportService.generateFromHtml(html);
        emailService.sendReport(pdf);
    }
}