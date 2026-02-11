package com.example.offermanagementsystem.controller;

import com.example.offermanagementsystem.model.*;
import com.example.offermanagementsystem.repository.OfferRepository;
import com.example.offermanagementsystem.repository.OfferStatusHistoryRepository;
import com.example.offermanagementsystem.service.ActorResolverService;
import com.example.offermanagementsystem.service.AuditService;
import com.example.offermanagementsystem.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    private final OfferRepository offerRepository;
    private final OfferStatusHistoryRepository historyRepository;
    private final EmailService emailService;
    private final AuditService auditService;
    private final ActorResolverService actorResolverService;

    public CustomerController(
            OfferRepository offerRepository,
            OfferStatusHistoryRepository historyRepository,
            EmailService emailService,
            AuditService auditService,
            ActorResolverService actorResolverService
    ) {
        this.offerRepository = offerRepository;
        this.historyRepository = historyRepository;
        this.emailService = emailService;
        this.auditService = auditService;
        this.actorResolverService = actorResolverService;
    }

    // ===============================
    // DETAIL NABÍDKY (TOKEN)
    // ===============================
    @GetMapping("/{token}")
    public String customerOffer(
            @PathVariable String token,
            Model model,
            HttpServletRequest request
    ) {

        Offer offer = offerRepository
                .findByCustomerToken(token)
                .orElseThrow(() -> new RuntimeException("Nabídka nenalezena"));

        model.addAttribute("offer", offer);

        // Audit – zákazník otevřel nabídku
        auditService.log(
                offer,
                AuditAction.VIEW,
                AuditSection.CUSTOMER,
                actorResolverService.resolveActor(), // může být null (token access)
                "Zákazník otevřel nabídku",
                null,
                null,
                request
        );

        return "customer/detail";
    }

    // ===============================
    // ACCEPT
    // ===============================
    @PostMapping("/{token}/accept")
    public String accept(
            @PathVariable String token,
            HttpServletRequest request
    ) {

        Offer offer = offerRepository
                .findByCustomerToken(token)
                .orElseThrow(() -> new RuntimeException("Nabídka nenalezena"));

        if (offer.getStatus() != OfferStatus.ODESLANA) {
            return "redirect:/customer/" + token + "?error=invalid";
        }

        changeStatus(
                offer,
                OfferStatus.PRIJATA,
                "Zákazník přijal nabídku",
                request
        );

        return "redirect:/customer/" + token + "?success=accepted";
    }

    // ===============================
    // REJECT
    // ===============================
    @PostMapping("/{token}/reject")
    public String reject(
            @PathVariable String token,
            @RequestParam(required = false) String note,
            HttpServletRequest request
    ) {

        Offer offer = offerRepository
                .findByCustomerToken(token)
                .orElseThrow(() -> new RuntimeException("Nabídka nenalezena"));

        if (offer.getStatus() != OfferStatus.ODESLANA) {
            return "redirect:/customer/" + token + "?error=invalid";
        }

        String finalNote = (note == null || note.isBlank())
                ? "Zákazník zamítl nabídku"
                : note;

        changeStatus(
                offer,
                OfferStatus.ZAMITNUTA,
                finalNote,
                request
        );

        return "redirect:/customer/" + token + "?success=rejected";
    }

    // ===============================
    // HELPER
    // ===============================
    private void changeStatus(
            Offer offer,
            OfferStatus newStatus,
            String note,
            HttpServletRequest request
    ) {

        OfferStatus oldStatus = offer.getStatus();
        User actor = actorResolverService.resolveActor(); // null při token přístupu

        // ===== STATUS HISTORY =====
        OfferStatusHistory history = new OfferStatusHistory();
        history.setOffer(offer);
        history.setFromStatus(oldStatus);
        history.setToStatus(newStatus);
        history.setChangedBy(actor); // může být null
        history.setChangedAt(LocalDateTime.now());
        history.setNote(note);

        offer.setStatus(newStatus);
        offer.setInEdit(false);

        offerRepository.save(offer);
        historyRepository.save(history);

        // ===== CENTRAL AUDIT =====
        AuditAction action =
                newStatus == OfferStatus.PRIJATA
                        ? AuditAction.ACCEPT
                        : AuditAction.REJECT;

        auditService.log(
                offer,
                action,
                AuditSection.CUSTOMER,
                actor,
                note,
                oldStatus.name(),
                newStatus.name(),
                request
        );

        try {
            emailService.sendStatusEmailSafe(offer);
        } catch (Exception e) {
            System.err.println("Email error: " + e.getMessage());
        }
    }
}