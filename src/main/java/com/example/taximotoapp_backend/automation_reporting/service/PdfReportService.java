package com.example.taximotoapp_backend.automation_reporting.service;

import com.example.taximotoapp_backend.automation_reporting.dto.DailyReportDTO;
import com.example.taximotoapp_backend.automation_reporting.dto.TopDriverDTO;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

@Service
public class PdfReportService {
    public byte[] generateDailyReport(DailyReportDTO dto) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("MotoTaxi Daily Report"));
            document.add(new Paragraph("Date: " + LocalDate.now()));
            document.add(new Paragraph("----------------------------------"));

            document.add(new Paragraph("Revenue Today: " + dto.getTodayRevenue()));
            document.add(new Paragraph("Total Trips: " + dto.getTotalTrips()));
            document.add(new Paragraph("Inactive Drivers: " + dto.getInactiveDrivers()));
            document.add(new Paragraph("New Clients: " + dto.getNewClients()));
            document.add(new Paragraph("Cancellation Rate: " + dto.getCancellationRate() + "%"));

            document.add(new Paragraph("Cash Payments: " + dto.getPaymentStats().getCashPayments()));
            document.add(new Paragraph("Online Payments: " + dto.getPaymentStats().getOnlinePayments()));

            document.add(new Paragraph("----------------------------------"));
            document.add(new Paragraph("Top Drivers"));

            for (TopDriverDTO d : dto.getTopDrivers()) {
                document.add(new Paragraph(
                        d.getDriverName()
                                + " | Revenue: " + d.getRevenue()
                                + " | Trips: " + d.getTrips()
                ));
            }

            document.add(new Paragraph("----------------------------------"));
            document.add(new Paragraph("AI Analysis"));
            document.add(new Paragraph(dto.getAiAnalysis()));

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}