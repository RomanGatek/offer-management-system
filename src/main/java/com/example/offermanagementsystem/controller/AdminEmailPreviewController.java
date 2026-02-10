package com.example.offermanagementsystem.controller;

import com.example.offermanagementsystem.model.Offer;
import com.example.offermanagementsystem.repository.OfferRepository;
import com.example.offermanagementsystem.service.EmailService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/offers")
public class AdminEmailPreviewController {

    private final OfferRepository offerRepository;
    private final EmailService emailService;

    public AdminEmailPreviewController(
            OfferRepository offerRepository,
            EmailService emailService
    ) {
        this.offerRepository = offerRepository;
        this.emailService = emailService;
    }

    // --------------------------------------------------
    // EMAIL PREVIEW
    // --------------------------------------------------
    @GetMapping("/{id}/email/preview")
    public String previewEmail(
            @PathVariable Long id,
            @RequestParam String type,
            Model model
    ) {
        Offer offer = offerRepository.findById(id).orElseThrow();

        String subject;
        String template;

        switch (type) {
            case "status" -> {
                subject = emailService.previewSubjectForStatus(offer);
                template = "mail/status :: this";
            }
            case "reminder7" -> {
                subject = "Připomenutí nabídky";
                template = "mail/reminder-7 :: this";
            }
            case "reminder14" -> {
                subject = "Poslední připomenutí nabídky";
                template = "mail/reminder-14 :: this";
            }
            case "expired" -> {
                subject = "Platnost nabídky vypršela";
                template = "mail/expired :: this";
            }
            default -> throw new IllegalArgumentException("Neznámý typ emailu");
        }

        String html =
                emailService.renderEmailPreview(
                        offer,
                        subject,
                        template
                );

        model.addAttribute("subject", subject);
        model.addAttribute("html", html);

        return "admin/email-preview";
    }
}