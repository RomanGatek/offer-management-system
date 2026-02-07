package com.example.offermanagementsystem.controller;

import com.example.offermanagementsystem.model.*;
import com.example.offermanagementsystem.repository.*;
import com.example.offermanagementsystem.service.EmailService;
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

    public CustomerController(
            OfferRepository offerRepository,
            OfferStatusHistoryRepository historyRepository,
            EmailService emailService) {

        this.offerRepository = offerRepository;
        this.historyRepository = historyRepository;
        this.emailService = emailService;
    }

    // ===============================
    // DETAIL NABÍDKY (TOKEN)
    // ===============================
    @GetMapping("/{token}")
    public String customerOffer(@PathVariable String token, Model model) {

        Offer offer = offerRepository
                .findByCustomerToken(token)
                .orElseThrow();

        model.addAttribute("offer", offer);
        return "customer/detail";
    }

    // ===============================
    // ACCEPT
    // ===============================
    @PostMapping("/{token}/accept")
    public String accept(@PathVariable String token) {

        Offer offer = offerRepository
                .findByCustomerToken(token)
                .orElseThrow();

        // ochrana proti vícenásobnému kliknutí
        if (offer.getStatus() != OfferStatus.ODESLANA) {
            return "redirect:/customer/" + token + "?error=invalid";
        }

        changeStatus(
                offer,
                OfferStatus.PRIJATA,
                "Zákazník přijal nabídku"
        );

        return "redirect:/customer/" + token + "?success=accepted";
    }

    // ===============================
    // REJECT
    // ===============================
    @PostMapping("/{token}/reject")
    public String reject(
            @PathVariable String token,
            @RequestParam(required = false) String note) {

        Offer offer = offerRepository
                .findByCustomerToken(token)
                .orElseThrow();

        if (offer.getStatus() != OfferStatus.ODESLANA) {
            return "redirect:/customer/" + token + "?error=invalid";
        }

        String finalNote = (note == null || note.isBlank())
                ? "Zákazník zamítl nabídku"
                : note;

        changeStatus(
                offer,
                OfferStatus.ZAMITNUTA,
                finalNote
        );

        return "redirect:/customer/" + token + "?success=rejected";
    }

    // ===============================
    // HELPER
    // ===============================
    private void changeStatus(
            Offer offer,
            OfferStatus newStatus,
            String note) {

        OfferStatusHistory h = new OfferStatusHistory();
        h.setOffer(offer);
        h.setFromStatus(offer.getStatus());
        h.setToStatus(newStatus);
        h.setChangedBy(null); // 🔹 změnu provedl zákazník
        h.setChangedAt(LocalDateTime.now());
        h.setNote(note);

        offer.setStatus(newStatus);
        offer.setInEdit(false); // 🔓 jistota

        offerRepository.save(offer);
        historyRepository.save(h);

        emailService.sendStatusEmailSafe(offer);
    }
}