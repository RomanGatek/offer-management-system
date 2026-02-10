package com.example.offermanagementsystem.controller;

import com.example.offermanagementsystem.model.*;
import com.example.offermanagementsystem.repository.OfferAccessLogRepository;
import com.example.offermanagementsystem.repository.OfferRepository;
import com.example.offermanagementsystem.repository.OfferStatusHistoryRepository;
import com.example.offermanagementsystem.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/public")
public class PublicOfferController {

    private final OfferRepository offerRepository;
    private final OfferStatusHistoryRepository historyRepository;
    private final OfferAccessLogRepository accessLogRepository;
    private final EmailService emailService;

    public PublicOfferController(
            OfferRepository offerRepository,
            OfferStatusHistoryRepository historyRepository,
            OfferAccessLogRepository accessLogRepository,
            EmailService emailService
    ) {
        this.offerRepository = offerRepository;
        this.historyRepository = historyRepository;
        this.accessLogRepository = accessLogRepository;
        this.emailService = emailService;
    }

    // ===============================
    // VEŘEJNÝ DETAIL NABÍDKY
    // ===============================
    @GetMapping("/offers/{token}")
    public String publicOfferDetail(
            @PathVariable String token,
            Model model,
            HttpServletRequest request
    ) {
        Offer offer = offerRepository
                .findByCustomerToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Neplatný odkaz"));

        if (!offer.isTokenValid()) {
            model.addAttribute("expiredAt", offer.getTokenExpiresAt());
            return "public/offer-expired";
        }

        if (!accessLogRepository.existsByOfferAndAction(offer, AuditAction.VIEW)) {
            logAccess(offer, AuditAction.VIEW, request);
        }

        model.addAttribute("offer", offer);
        return "public/offer-detail";
    }

    // ===============================
    // 📜 VEŘEJNÁ TIMELINE (M)
    // ===============================
    @GetMapping("/offers/{token}/timeline")
    public String publicTimeline(@PathVariable String token, Model model) {

        Offer offer = offerRepository
                .findByCustomerToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Neplatný odkaz"));

        List<OfferAccessLog> logs =
                accessLogRepository.findByOfferOrderByAccessedAtAsc(offer);

        model.addAttribute("offer", offer);
        model.addAttribute("logs", logs);

        return "public/offer-timeline";
    }

    // ===============================
    // PŘIJETÍ NABÍDKY
    // ===============================
    @PostMapping("/offers/{token}/accept")
    public String acceptOffer(
            @PathVariable String token,
            HttpServletRequest request
    ) {
        Offer offer = offerRepository.findByCustomerToken(token).orElseThrow();

        if (!offer.isTokenValid() || offer.getStatus() != OfferStatus.ODESLANA) {
            return "redirect:/public/offers/" + token;
        }

        logAccess(offer, AuditAction.ACCEPT, request);
        changeStatus(offer, OfferStatus.PRIJATA, "Zákazník přijal nabídku");

        return "redirect:/public/offers/" + token + "?accepted";
    }

    // ===============================
    // ZAMÍTNUTÍ NABÍDKY
    // ===============================
    @PostMapping("/offers/{token}/reject")
    public String rejectOffer(
            @PathVariable String token,
            @RequestParam(required = false) String note,
            HttpServletRequest request
    ) {
        Offer offer = offerRepository.findByCustomerToken(token).orElseThrow();

        if (!offer.isTokenValid() || offer.getStatus() != OfferStatus.ODESLANA) {
            return "redirect:/public/offers/" + token;
        }

        logAccess(offer, AuditAction.REJECT, request);

        changeStatus(
                offer,
                OfferStatus.ZAMITNUTA,
                note != null ? note : "Zákazník nabídku zamítl"
        );

        return "redirect:/public/offers/" + token + "?rejected";
    }

    // ===============================
    // HELPERS
    // ===============================
    private void logAccess(
            Offer offer,
            AuditAction action,
            HttpServletRequest request
    ) {
        OfferAccessLog log = new OfferAccessLog();
        log.setOffer(offer);
        log.setAction(action);
        log.setAccessedAt(LocalDateTime.now());
        log.setIpAddress(request.getRemoteAddr());
        log.setUserAgent(request.getHeader("User-Agent"));
        accessLogRepository.save(log);
    }

    private void changeStatus(
            Offer offer,
            OfferStatus newStatus,
            String note
    ) {
        OfferStatusHistory h = new OfferStatusHistory();
        h.setOffer(offer);
        h.setFromStatus(offer.getStatus());
        h.setToStatus(newStatus);
        h.setChangedAt(LocalDateTime.now());
        h.setNote(note);

        offer.setStatus(newStatus);

        offerRepository.save(offer);
        historyRepository.save(h);

        emailService.sendStatusEmailSafe(offer);
    }
}