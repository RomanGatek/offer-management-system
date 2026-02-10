package com.example.offermanagementsystem.service;

import com.example.offermanagementsystem.model.*;
import com.example.offermanagementsystem.repository.OfferAccessLogRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final OfferAccessLogRepository accessLogRepository;

    private static final String BASE_URL = "http://localhost:8080";

    public EmailService(
            JavaMailSender mailSender,
            TemplateEngine templateEngine,
            OfferAccessLogRepository accessLogRepository
    ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.accessLogRepository = accessLogRepository;
    }

    // ===============================
    // VEŘEJNÉ API – SEND
    // ===============================
    public void sendStatusEmailSafe(Offer offer) {
        try {
            sendHtmlEmail(
                    offer,
                    subjectForStatus(offer),
                    "mail/status :: this",
                    AuditAction.EMAIL_SENT
            );
        } catch (Exception e) {
            logError("status", offer, e);
        }
    }

    public void sendCustomerReminderSafe(Offer offer, int days) {
        try {
            AuditAction action =
                    days >= 14 ? AuditAction.REMINDER_14 : AuditAction.REMINDER_7;

            String template =
                    days >= 14
                            ? "mail/reminder-14 :: this"
                            : "mail/reminder-7 :: this";

            sendHtmlEmail(
                    offer,
                    reminderSubject(days),
                    template,
                    action
            );
        } catch (Exception e) {
            logError("reminder", offer, e);
        }
    }

    public void sendExpirationEmailSafe(Offer offer) {
        try {
            sendHtmlEmail(
                    offer,
                    "Platnost nabídky vypršela",
                    "mail/expired :: this",
                    AuditAction.EXPIRED
            );
        } catch (Exception e) {
            logError("expiration", offer, e);
        }
    }

    // ======================================================
    // 🔍 PREVIEW – ADMIN (CHYBĚJÍCÍ METODA)
    // ======================================================
    /**
     * Používá AdminEmailPreviewController
     */
    public String previewSubjectForStatus(Offer offer) {
        return subjectForStatus(offer);
    }

    // ===============================
    // SEND ENGINE
    // ===============================
    private void sendHtmlEmail(
            Offer offer,
            String subject,
            String contentTemplate,
            AuditAction auditAction
    ) throws MessagingException {

        String html = renderEmailPreview(offer, subject, contentTemplate);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper =
                new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(offer.getCustomerEmail());
        helper.setSubject(subject);
        helper.setText(html, true);

        mailSender.send(message);

        // ===== AUDIT LOG =====
        OfferAccessLog log = new OfferAccessLog();
        log.setOffer(offer);
        log.setAction(auditAction);
        log.setAccessedAt(LocalDateTime.now());
        log.setUserAgent("EMAIL");

        accessLogRepository.save(log);
    }

    // ===============================
    // TEMPLATE
    // ===============================
    public String renderEmailPreview(
            Offer offer,
            String subject,
            String contentTemplate
    ) {
        Context context = new Context();
        context.setVariable("offer", offer);
        context.setVariable("subject", subject);
        context.setVariable("contentTemplate", contentTemplate);
        context.setVariable(
                "publicUrl",
                BASE_URL + "/public/offers/" + offer.getCustomerToken()
        );
        context.setVariable(
                "trackingPixelUrl",
                BASE_URL + "/track/open/" + offer.getCustomerToken()
        );

        return templateEngine.process("mail/base", context);
    }

    // ===============================
    // SUBJECTY
    // ===============================
    private String subjectForStatus(Offer offer) {
        return switch (offer.getStatus()) {
            case ODESLANA -> "Nová nabídka k potvrzení";
            case PRIJATA -> "Nabídka byla přijata";
            case ZAMITNUTA -> "Nabídka byla zamítnuta";
            default -> "Změna stavu nabídky";
        };
    }

    private String reminderSubject(int days) {
        return days >= 14
                ? "Poslední připomenutí nabídky"
                : "Připomenutí nabídky";
    }

    // ===============================
    // LOGGING
    // ===============================
    private void logError(String type, Offer offer, Exception e) {
        System.err.println(
                "Nepodařilo se odeslat " + type +
                        " email k nabídce ID=" + offer.getId() +
                        ": " + e.getMessage()
        );
    }
}