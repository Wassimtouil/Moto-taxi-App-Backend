package com.example.taximotoapp_backend.automation_reporting.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // Tu peux externaliser ça dans application.properties
    private static final String ADMIN_EMAIL = "wassimtouil23@gmail.com";

    public void sendReport(byte[] pdfBytes) {

        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new RuntimeException("PDF report is empty");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();

            // true = multipart (pour attachment)
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(ADMIN_EMAIL);
            helper.setSubject("MotoTaxi - Daily Report");
            helper.setText(buildEmailBody(), false);

            ByteArrayResource attachment = new ByteArrayResource(pdfBytes);

            helper.addAttachment(
                    "daily-report.pdf",
                    attachment
            );

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email report: " + e.getMessage(), e);
        }
    }

    private String buildEmailBody() {

        return """
                Hello Admin,

                Please find attached the daily automation report for MotoTaxi.

                This report includes:
                - Revenue statistics
                - Trips analysis
                - Driver performance
                - Payments breakdown
                - Client activity
                - Reclamation AI analysis

                Regards,
                MotoTaxi Automation System
                """;
    }
}
