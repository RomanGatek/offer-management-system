package com.example.offermanagementsystem.service;

import com.example.offermanagementsystem.model.Offer;
import com.example.offermanagementsystem.model.OfferStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    private static final String BASE_URL = "http://localhost:8080";

    public EmailService(
            JavaMailSender mailSender,
            TemplateEngine templateEngine
    ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    // ======================================================
    // VEŘEJNÉ API – SEND
    // ======================================================

    public void sendStatusEmailSafe(Offer offer) {
        try {
            sendHtmlEmail(
                    offer,
                    subjectForStatus(offer),
                    "mail/status :: this"
            );
        } catch (Exception e) {
            logError("status", offer, e);
        }
    }

    public void sendCustomerReminderSafe(Offer offer) {
        sendCustomerReminderSafe(offer, 7);
    }

    public void sendCustomerReminderSafe(Offer offer, int days) {
        try {
            String template =
                    days >= 14
                            ? "mail/reminder-14 :: this"
                            : "mail/reminder-7 :: this";

            sendHtmlEmail(
                    offer,
                    reminderSubject(days),
                    template
            );
        } catch (Exception e) {
            logError("reminder " + days, offer, e);
        }
    }

    public void sendExpirationEmailSafe(Offer offer) {
        try {
            sendHtmlEmail(
                    offer,
                    "Platnost nabídky vypršela",
                    "mail/expired :: this"
            );
        } catch (Exception e) {
            logError("expiration", offer, e);
        }
    }

    // ======================================================
    // PREVIEW (ADMIN)
    // ======================================================

    public String renderEmailPreview(
            Offer offer,
            String subject,
            String contentTemplate
    ) {
        String publicUrl =
                BASE_URL + "/public/offers/" + offer.getCustomerToken();

        Context context = new Context();
        context.setVariable("offer", offer);
        context.setVariable("publicUrl", publicUrl);
        context.setVariable("subject", subject);
        context.setVariable("contentTemplate", contentTemplate);

        return templateEngine.process("mail/base", context);
    }

    public String previewSubjectForStatus(Offer offer) {
        return subjectForStatus(offer);
    }

    // ======================================================
    // INTERNÍ SEND ENGINE
    // ======================================================

    private void sendHtmlEmail(
            Offer offer,
            String subject,
            String contentTemplate
    ) throws MessagingException {

        String html =
                renderEmailPreview(offer, subject, contentTemplate);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper =
                new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(offer.getCustomerEmail());
        helper.setSubject(subject);
        helper.setText(html, true);

        mailSender.send(message);
    }

    // ======================================================
    // SUBJECTY
    // ======================================================

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

    // ======================================================
    // LOGGING
    // ======================================================

    private void logError(String type, Offer offer, Exception e) {
        System.err.println(
                "Nepodařilo se odeslat " + type +
                        " email k nabídce ID=" + offer.getId() +
                        ": " + e.getMessage()
        );
    }
}