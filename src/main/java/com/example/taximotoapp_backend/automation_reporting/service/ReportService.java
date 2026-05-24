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

                // Analytique courses
                .avgTripPrice(statisticsService.getAvgTripPrice())
                .avgDistance(statisticsService.getAvgDistance())
                .totalDistance(statisticsService.getTotalDistance())
                .avgDuration(statisticsService.getAvgDuration())

                // Finance
                .paymentStats(statisticsService.getPaymentStats())
                .cashRevenue(statisticsService.getCashRevenue())
                .onlineRevenue(statisticsService.getOnlineRevenue())

                // Heures de pointe & zones
                .peakHours(statisticsService.getPeakHours())
                .topZones(statisticsService.getTopZones())

                // Top chauffeurs & réclamations
                .topDrivers(statisticsService.getTopDrivers())
                .reclamations(statisticsService.getTodayReclamations())

                // Système
                .blockedUsers(statisticsService.getBlockedUsers())
                .pendingDrivers(statisticsService.getPendingDrivers())
                .reclamationCount(statisticsService.getReclamationCount())

                // Comparaison hier
                .yesterdayRevenue(statisticsService.getYesterdayRevenue())
                .yesterdayTrips(statisticsService.getYesterdayTrips())
                .build();

        // ── Construction du contexte Thymeleaf ──────────────────────
        Context context = new Context();
        context.setVariable("date",              LocalDate.now().toString());

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

        // Analytique courses
        context.setVariable("avgTripPrice",      dto.getAvgTripPrice());
        context.setVariable("avgDistance",       dto.getAvgDistance());
        context.setVariable("totalDistance",     dto.getTotalDistance());
        context.setVariable("avgDuration",       dto.getAvgDuration());

        // Finance
        context.setVariable("paymentStats",      dto.getPaymentStats());
        context.setVariable("cashRevenue",       dto.getCashRevenue());
        context.setVariable("onlineRevenue",     dto.getOnlineRevenue());

        // Heures de pointe & zones
        context.setVariable("peakHours",         dto.getPeakHours());
        context.setVariable("topZones",          dto.getTopZones());

        // Top chauffeurs & réclamations
        context.setVariable("topDrivers",        dto.getTopDrivers());
        context.setVariable("reclamations",      dto.getReclamations());

        // Système
        context.setVariable("blockedUsers",      dto.getBlockedUsers());
        context.setVariable("pendingDrivers",    dto.getPendingDrivers());
        context.setVariable("reclamationCount",  dto.getReclamationCount());

        // Comparaison hier
        context.setVariable("yesterdayRevenue",  dto.getYesterdayRevenue());
        context.setVariable("yesterdayTrips",    dto.getYesterdayTrips());

        // ── Génération HTML → PDF → Email ───────────────────────────
        String html = templateEngine.process("report", context);
        byte[] pdf  = pdfReportService.generateFromHtml(html);
        emailService.sendReport(pdf);
    }
}