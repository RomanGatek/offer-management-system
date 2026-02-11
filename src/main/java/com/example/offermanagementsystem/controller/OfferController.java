package com.example.offermanagementsystem.controller;

import com.example.offermanagementsystem.model.*;
import com.example.offermanagementsystem.repository.*;
import com.example.offermanagementsystem.service.AuditService;
import com.example.offermanagementsystem.service.EmailService;
import com.example.offermanagementsystem.service.PdfExportService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/offers")
public class OfferController {

    private final OfferRepository offerRepository;
    private final UserRepository userRepository;
    private final OfferStatusHistoryRepository historyRepository;
    private final OfferRevisionRepository revisionRepository;
    private final PdfExportService pdfExportService;
    private final EmailService emailService;
    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;


    public OfferController(
            OfferRepository offerRepository,
            UserRepository userRepository,
            OfferStatusHistoryRepository historyRepository,
            OfferRevisionRepository revisionRepository,
            PdfExportService pdfExportService,
            EmailService emailService,
            AuditLogRepository auditLogRepository,
            OfferAccessLogRepository offerAccessLogRepository,
            AuditService auditService

    ) {
        this.offerRepository = offerRepository;
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
        this.revisionRepository = revisionRepository;
        this.pdfExportService = pdfExportService;
        this.emailService = emailService;
        this.auditLogRepository = auditLogRepository;
        this.auditService = auditService;
    }

    // ===============================
    // USER DASHBOARD – vlastní nabídky
    // ===============================
    @GetMapping
    public String userOffers(Model model, Principal principal) {

        User user = getCurrentUser(principal);

        // ⛔ ADMIN sem nepatří
        if (isAdmin(user)) {
            return "redirect:/admin";
        }

        model.addAttribute(
                "offers",
                offerRepository.findByUserAndArchivedFalse(user)
        );
        model.addAttribute("user", user);

        return "dashboard";
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
            Principal principal
    ) {
        if (result.hasErrors()) {
            return "offers/new";
        }

        User user = getCurrentUser(principal);

        offer.setUser(user);
        offer.setStatus(OfferStatus.NOVA);
        offer.setRevision(1);
        offer.setInEdit(false);
        offer.setArchived(false);

        // ✅ TOKEN PLATNÝ 30 DNÍ
        offer.setTokenExpiresAt(LocalDateTime.now().plusDays(30));

        offerRepository.save(offer);
        return "redirect:/offers";
    }

    // ===============================
    // DETAIL – pouze vlastník
    // ===============================
    @GetMapping("/detail/{id}")
    public String offerDetail(
            @PathVariable Long id,
            Model model,
            Principal principal
    ) {
        Offer offer = offerRepository.findById(id).orElseThrow();
        User user = getCurrentUser(principal);

        if (!offer.getUser().getId().equals(user.getId())) {
            return "redirect:/offers?error=unauthorized";
        }

        model.addAttribute("offer", offer);
        model.addAttribute(
                "history",
                historyRepository.findByOfferOrderByChangedAtDesc(offer)
        );
        model.addAttribute(
                "revisions",
                revisionRepository.findByOfferOrderByRevisionNumberDesc(offer)
        );

        return "offers/detail";
    }

    // ===============================
// AUDIT – vlastník + admin
// ===============================
    @GetMapping("/{id}/audit")
    public String offerAudit(
            @PathVariable Long id,
            Principal principal,
            Model model
    ) {

        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found"));

        User user = getCurrentUser(principal);

        if (!isAdmin(user) &&
                !offer.getUser().getId().equals(user.getId())) {
            return "redirect:/offers?error=unauthorized";
        }

        model.addAttribute("offer", offer);
        model.addAttribute(
                "logs",
                auditService.getOfferAudit(id)
        );

        return "offers/audit";
    }

    // ===============================
    // SEND – NOVA → ODESLANA
    // ===============================
    @PostMapping("/{id}/send")
    public String sendOffer(@PathVariable Long id, Principal principal) {

        Offer offer = offerRepository.findById(id).orElseThrow();
        User user = getCurrentUser(principal);

        if (!offer.getUser().getId().equals(user.getId())
                || offer.getStatus() != OfferStatus.NOVA) {
            return "redirect:/offers?error=invalid-action";
        }

        offer.setStatus(OfferStatus.ODESLANA);
        offer.setInEdit(false);
        offerRepository.save(offer);

        saveHistory(
                offer,
                OfferStatus.NOVA,
                OfferStatus.ODESLANA,
                null,
                user
        );

        emailService.sendStatusEmailSafe(offer);

        return "redirect:/offers/detail/" + id;
    }

    // ===============================
    // REQUEST FIX – ODESLANA → K_UPRAVE
    // ===============================
    @PostMapping("/{id}/request-fix")
    public String requestFix(
            @PathVariable Long id,
            @RequestParam String note,
            Principal principal
    ) {
        Offer offer = offerRepository.findById(id).orElseThrow();
        User user = getCurrentUser(principal);

        if (!offer.getUser().getId().equals(user.getId())
                || offer.getStatus() != OfferStatus.ODESLANA) {
            return "redirect:/offers?error=invalid-action";
        }

        // 📌 uložit revizi
        OfferRevision rev = new OfferRevision();
        rev.setOffer(offer);
        rev.setRevisionNumber(offer.getRevision());
        rev.setCustomerName(offer.getCustomerName());
        rev.setCustomerEmail(offer.getCustomerEmail());
        rev.setDescription(offer.getDescription());
        rev.setTotalPrice(offer.getTotalPrice());
        rev.setCreatedAt(LocalDateTime.now());
        rev.setCreatedBy(user);
        revisionRepository.save(rev);

        offer.setStatus(OfferStatus.K_UPRAVE);
        offer.setRevision(offer.getRevision() + 1);
        offer.setInEdit(false);
        offerRepository.save(offer);

        saveHistory(
                offer,
                OfferStatus.ODESLANA,
                OfferStatus.K_UPRAVE,
                note,
                user
        );

        return "redirect:/offers/detail/" + id;
    }

    // ===============================
    // PDF – jen vlastník
    // ===============================
    @GetMapping("/export/{id}")
    public void exportPdf(
            @PathVariable Long id,
            HttpServletResponse response,
            Principal principal
    ) throws IOException {

        Offer offer = offerRepository.findById(id).orElseThrow();
        User user = getCurrentUser(principal);

        if (!offer.getUser().getId().equals(user.getId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

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
    private void saveHistory(
            Offer offer,
            OfferStatus from,
            OfferStatus to,
            String note,
            User user
    ) {
        OfferStatusHistory h = new OfferStatusHistory();
        h.setOffer(offer);
        h.setFromStatus(from);
        h.setToStatus(to);
        h.setChangedBy(user);
        h.setChangedAt(LocalDateTime.now());
        h.setNote(note);
        historyRepository.save(h);
    }

    private User getCurrentUser(Principal principal) {
        return userRepository
                .findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    private boolean isAdmin(User user) {
        return "ROLE_ADMIN".equals(user.getRole())
                || "ADMIN".equals(user.getRole());
    }
}