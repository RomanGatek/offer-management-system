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
    @Autowired private OfferRevisionRepository revisionRepository;
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
            @Valid @ModelAttribute("offer") Offer offer,
            BindingResult result,
            Principal principal) {

        if (result.hasErrors()) {
            return "offers/new";
        }

        offer.setUser(getCurrentUser(principal));
        offer.setStatus(OfferStatus.NOVA);
        offer.setRevision(1);
        offer.setInEdit(false);

        offerRepository.save(offer);
        return "redirect:/offers";
    }

    // ===============================
    // DETAIL
    // ===============================
    @GetMapping("/detail/{id}")
    public String offerDetail(@PathVariable Long id, Model model, Principal principal) {

        Offer offer = offerRepository.findById(id).orElseThrow();
        User user = getCurrentUser(principal);

        if (!offer.getUser().equals(user)) {
            return "redirect:/offers?error=unauthorized";
        }

        model.addAttribute("offer", offer);
        model.addAttribute("history",
                historyRepository.findByOfferOrderByChangedAtDesc(offer));
        model.addAttribute("revisions",
                revisionRepository.findByOfferOrderByRevisionNumberDesc(offer));

        return "offers/detail";
    }

    // ===============================
    // EDIT (LOCK + REVIZE)
    // ===============================
    @GetMapping("/edit/{id}")
    public String editOffer(@PathVariable Long id, Model model, Principal principal) {

        Offer offer = offerRepository.findById(id).orElseThrow();
        User user = getCurrentUser(principal);

        if (!canEditOffer(user, offer)) {
            return "redirect:/offers?error=unauthorized";
        }

        // 🔒 zákaz paralelní editace
        if (offer.isInEdit()) {
            return "redirect:/offers/detail/" + id + "?error=locked";
        }

        // 📌 revize pouze při přechodu ODESLANA → K_UPRAVE
        if (offer.getStatus() == OfferStatus.ODESLANA) {

            OfferRevision rev = new OfferRevision();
            rev.setOffer(offer);
            rev.setRevisionNumber(offer.getRevision());
            rev.setCustomerName(offer.getCustomerName());
            rev.setCustomerEmail(offer.getCustomerEmail());
            rev.setDescription(offer.getDescription());
            rev.setTotalPrice(offer.getTotalPrice()); // BigDecimal → BigDecimal
            rev.setCreatedAt(LocalDateTime.now());
            rev.setCreatedBy(user);

            revisionRepository.save(rev);

            offer.setStatus(OfferStatus.K_UPRAVE);
            offer.setRevision(offer.getRevision() + 1);
        }

        offer.setInEdit(true);
        offerRepository.save(offer);

        model.addAttribute("offer", offer);
        return "offers/edit";
    }

    // ===============================
    // UPDATE
    // ===============================
    @PostMapping("/update/{id}")
    public String updateOffer(
            @PathVariable Long id,
            @Valid @ModelAttribute("offer") Offer updated,
            BindingResult result,
            Principal principal) {

        Offer offer = offerRepository.findById(id).orElseThrow();

        if (!canEditOffer(getCurrentUser(principal), offer)) {
            return "redirect:/offers?error=unauthorized";
        }

        if (result.hasErrors()) {
            return "offers/edit";
        }

        offer.setCustomerName(updated.getCustomerName());
        offer.setCustomerEmail(updated.getCustomerEmail());
        offer.setDescription(updated.getDescription());
        offer.setTotalPrice(updated.getTotalPrice());
        offer.setInEdit(false);

        offerRepository.save(offer);
        return "redirect:/offers/detail/" + id;
    }

    // ===============================
    // SEND
    // ===============================
    @PostMapping("/{id}/send")
    public String sendOffer(@PathVariable Long id, Principal principal) {

        Offer offer = offerRepository.findById(id).orElseThrow();

        if (!offer.getUser().equals(getCurrentUser(principal))
                || !(offer.getStatus() == OfferStatus.NOVA
                || offer.getStatus() == OfferStatus.K_UPRAVE)) {
            return "redirect:/offers?error=invalid-action";
        }

        changeStatus(offer, OfferStatus.ODESLANA, null);
        return "redirect:/offers";
    }

    // ===============================
    // ACCEPT / REJECT / REQUEST FIX
    // ===============================
    @PostMapping("/{id}/accept")
    public String accept(@PathVariable Long id) {

        changeStatus(
                offerRepository.findById(id).orElseThrow(),
                OfferStatus.PRIJATA,
                null
        );
        return "redirect:/offers";
    }

    @PostMapping("/{id}/reject")
    public String reject(
            @PathVariable Long id,
            @RequestParam(required = false) String note) {

        changeStatus(
                offerRepository.findById(id).orElseThrow(),
                OfferStatus.ZAMITNUTA,
                note
        );
        return "redirect:/offers";
    }

    @PostMapping("/{id}/request-fix")
    public String requestFix(
            @PathVariable Long id,
            @RequestParam String note) {

        changeStatus(
                offerRepository.findById(id).orElseThrow(),
                OfferStatus.K_UPRAVE,
                note
        );
        return "redirect:/offers/detail/" + id;
    }

    // ===============================
    // PDF
    // ===============================
    @GetMapping("/export/{id}")
    public void exportPdf(
            @PathVariable Long id,
            HttpServletResponse response) throws IOException {

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
    private void changeStatus(
            Offer offer,
            OfferStatus newStatus,
            String note) {

        OfferStatusHistory h = new OfferStatusHistory();
        h.setOffer(offer);
        h.setFromStatus(offer.getStatus());
        h.setToStatus(newStatus);
        h.setChangedBy(offer.getUser());
        h.setChangedAt(LocalDateTime.now());
        h.setNote(note);

        offer.setStatus(newStatus);
        offer.setInEdit(false); // 🔓 vždy odemknout

        offerRepository.save(offer);
        historyRepository.save(h);

        emailService.sendStatusEmailSafe(offer);
    }

    private User getCurrentUser(Principal principal) {
        return userRepository
                .findByUsername(principal.getName())
                .orElseThrow();
    }

    private boolean canEditOffer(User user, Offer offer) {
        return offer.getUser().equals(user)
                && (offer.getStatus() == OfferStatus.NOVA
                || offer.getStatus() == OfferStatus.K_UPRAVE
                || offer.getStatus() == OfferStatus.ODESLANA);
    }
}