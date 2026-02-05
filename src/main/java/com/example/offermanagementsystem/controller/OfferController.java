package com.example.offermanagementsystem.controller;

import com.example.offermanagementsystem.model.*;
import com.example.offermanagementsystem.repository.*;
import com.example.offermanagementsystem.service.EmailService;
import com.example.offermanagementsystem.service.PdfExportService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/offers")
public class OfferController {

    @Autowired private OfferRepository offerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private OfferStatusHistoryRepository historyRepository;
    @Autowired private PdfExportService pdfExportService;
    @Autowired private EmailService emailService;

    // ===============================
    // LIST
    // ===============================
    @GetMapping
    public String listOffers(Model model, Principal principal) {
        User user = getCurrentUser(principal);

        List<Offer> offers = user.getRole().equals("ADMIN")
                ? offerRepository.findAll()
                : offerRepository.findByUser(user);

        model.addAttribute("offers", offers);
        return "offers/list";
    }

    // ===============================
    // NEW
    // ===============================
    @GetMapping("/new")
    public String newOfferForm(Model model) {
        model.addAttribute("offer", new Offer());
        return "offers/new";
    }

    @PostMapping("/new")
    public String createOffer(
            @Valid @ModelAttribute Offer offer,
            BindingResult result,
            Principal principal
    ) {
        if (result.hasErrors()) {
            return "offers/new";
        }

        User user = getCurrentUser(principal);
        offer.setUser(user);
        offer.setStatus(OfferStatus.NOVA);

        offerRepository.save(offer);
        return "redirect:/offers";
    }

    // ===============================
    // DETAIL + AUDIT
    // ===============================
    @GetMapping("/{id}")
    public String offerDetail(@PathVariable Long id, Model model, Principal principal) {
        Offer offer = offerRepository.findById(id).orElseThrow();
        User user = getCurrentUser(principal);

        if (!canEditOffer(user, offer)) {
            return "redirect:/offers?error=unauthorized";
        }

        model.addAttribute("offer", offer);
        model.addAttribute(
                "history",
                historyRepository.findByOfferOrderByChangedAtDesc(offer)
        );

        return "offers/detail";
    }

    // ===============================
    // EDIT
    // ===============================
    @GetMapping("/edit/{id}")
    public String editOffer(@PathVariable Long id, Model model, Principal principal) {
        Offer offer = offerRepository.findById(id).orElseThrow();
        User user = getCurrentUser(principal);

        if (!canEditOffer(user, offer)) {
            return "redirect:/offers?error=unauthorized";
        }

        model.addAttribute("offer", offer);
        return "offers/edit";
    }

    @PostMapping("/update/{id}")
    public String updateOffer(
            @PathVariable Long id,
            @Valid @ModelAttribute Offer updatedOffer,
            BindingResult result,
            Principal principal,
            Model model
    ) {
        Offer offer = offerRepository.findById(id).orElseThrow();
        User user = getCurrentUser(principal);

        if (!canEditOffer(user, offer)) {
            return "redirect:/offers?error=unauthorized";
        }

        if (result.hasErrors()) {
            model.addAttribute("offer", offer);
            return "offers/edit";
        }

        offer.setCustomerName(updatedOffer.getCustomerName());
        offer.setCustomerEmail(updatedOffer.getCustomerEmail());
        offer.setDescription(updatedOffer.getDescription());
        offer.setTotalPrice(updatedOffer.getTotalPrice());

        offerRepository.save(offer);
        return "redirect:/offers";
    }

    // ===============================
    // STATUS ACTIONS
    // ===============================
    @PostMapping("/{id}/send")
    public String sendOffer(@PathVariable Long id, Principal principal) {
        Offer offer = offerRepository.findById(id).orElseThrow();
        User user = getCurrentUser(principal);

        if (!offer.getUser().equals(user) || offer.getStatus() != OfferStatus.NOVA) {
            return "redirect:/offers?error=invalid-action";
        }

        changeStatus(offer, OfferStatus.ODESLANA, user);
        return "redirect:/offers";
    }

    @PostMapping("/{id}/accept")
    public String acceptOffer(@PathVariable Long id, Principal principal) {
        Offer offer = offerRepository.findById(id).orElseThrow();
        User user = getCurrentUser(principal);

        if (!user.getRole().equals("ADMIN") || offer.getStatus() != OfferStatus.ODESLANA) {
            return "redirect:/offers?error=invalid-action";
        }

        changeStatus(offer, OfferStatus.PRIJATA, user);
        return "redirect:/offers";
    }

    @PostMapping("/{id}/reject")
    public String rejectOffer(@PathVariable Long id, Principal principal) {
        Offer offer = offerRepository.findById(id).orElseThrow();
        User user = getCurrentUser(principal);

        if (!user.getRole().equals("ADMIN") || offer.getStatus() != OfferStatus.ODESLANA) {
            return "redirect:/offers?error=invalid-action";
        }

        changeStatus(offer, OfferStatus.ZAMITNUTA, user);
        return "redirect:/offers";
    }

    // ===============================
    // PDF
    // ===============================
    @GetMapping("/export/{id}")
    public void exportPdf(@PathVariable Long id, HttpServletResponse response)
            throws IOException {

        Offer offer = offerRepository.findById(id).orElseThrow();

        response.setContentType("application/pdf");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=nabidka-" + id + ".pdf"
        );

        ByteArrayInputStream pdf =
                pdfExportService.generateOfferPdf(offer);

        pdf.transferTo(response.getOutputStream());
    }

    // ===============================
    // HELPERS
    // ===============================
    private void changeStatus(Offer offer, OfferStatus newStatus, User user) {
        OfferStatus oldStatus = offer.getStatus();
        offer.setStatus(newStatus);
        offerRepository.save(offer);

        OfferStatusHistory h = new OfferStatusHistory();
        h.setOffer(offer);
        h.setFromStatus(oldStatus);
        h.setToStatus(newStatus);
        h.setChangedBy(user);
        h.setChangedAt(LocalDateTime.now());
        historyRepository.save(h);

        emailService.sendStatusEmailSafe(offer);
    }

    private User getCurrentUser(Principal principal) {
        return userRepository.findByUsername(principal.getName()).orElseThrow();
    }

    private boolean canEditOffer(User user, Offer offer) {
        return user.getRole().equals("ADMIN") || offer.getUser().equals(user);
    }
}