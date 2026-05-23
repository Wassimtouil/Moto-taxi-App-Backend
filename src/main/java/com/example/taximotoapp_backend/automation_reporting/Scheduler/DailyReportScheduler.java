package com.example.taximotoapp_backend.automation_reporting.Scheduler;

import com.example.taximotoapp_backend.automation_reporting.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyReportScheduler {

    private final ReportService reportService;

    // chaque jour à 00:00
    @Scheduled(cron = "0 0 0 * * *")
    public void generateDailyReport() {

        System.out.println("DAILY REPORT STARTED");

        reportService.generateAndSendReport();
    }
}