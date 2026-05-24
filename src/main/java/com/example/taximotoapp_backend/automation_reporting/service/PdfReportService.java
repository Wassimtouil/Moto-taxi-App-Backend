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

    public byte[] generateFromHtml(String html) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder =
                    new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();

            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }
}