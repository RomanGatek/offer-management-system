package com.example.offermanagementsystem.controller;

import com.example.offermanagementsystem.model.AuditAction;
import com.example.offermanagementsystem.model.Offer;
import com.example.offermanagementsystem.model.OfferAccessLog;
import com.example.offermanagementsystem.model.OfferStatus;
import com.example.offermanagementsystem.repository.OfferAccessLogRepository;
import com.example.offermanagementsystem.repository.OfferRepository;
import com.example.offermanagementsystem.service.AuditActionLabelService;
import com.example.offermanagementsystem.service.EmailService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final int STALE_DAYS = 7;

    private final OfferRepository offerRepository;
    private final OfferAccessLogRepository accessLogRepository;
    private final EmailService emailService;
    private final AuditActionLabelService auditActionLabelService;

    public AdminController(
            OfferRepository offerRepository,
            OfferAccessLogRepository accessLogRepository,
            EmailService emailService,
            AuditActionLabelService auditActionLabelService
    ) {
        this.offerRepository = offerRepository;
        this.accessLogRepository = accessLogRepository;
        this.emailService = emailService;
        this.auditActionLabelService = auditActionLabelService;
    }

    // ======================================================
    // DASHBOARD
    // ======================================================
    @GetMapping
    public String adminDashboard(
            @RequestParam(defaultValue = "false") boolean showExpired,
            Model model
    ) {

        List<Offer> activeOffers = showExpired
                ? offerRepository.findByArchivedFalse()
                : offerRepository.findByArchivedFalseAndExpiredFalse();

        List<Offer> archivedOffers = offerRepository.findByArchivedTrue();

        long sentCount = offerRepository.countByStatus(OfferStatus.ODESLANA);
        long openedCount = offerRepository.countOpenedOffers();
        long acceptedCount = offerRepository.countAcceptedOffers();

        double openRate = sentCount == 0 ? 0 : (openedCount * 100.0 / sentCount);
        double acceptRate = openedCount == 0 ? 0 : (acceptedCount * 100.0 / openedCount);

        model.addAttribute("sentCount", sentCount);
        model.addAttribute("openedCount", openedCount);
        model.addAttribute("acceptedCount", acceptedCount);
        model.addAttribute("openRate", Math.round(openRate));
        model.addAttribute("acceptRate", Math.round(acceptRate));
        model.addAttribute("showExpired", showExpired);

        Map<Long, Long> viewsMap = new HashMap<>();
        Map<Long, LocalDateTime> lastViewedMap = new HashMap<>();
        Map<Long, AuditAction> reactionMap = new HashMap<>();
        Map<Long, Boolean> staleMap = new HashMap<>();
        Map<Long, Boolean> expiredMap = new HashMap<>();

        LocalDateTime staleLimit = LocalDateTime.now().minusDays(STALE_DAYS);

        for (Offer offer : activeOffers) {

            long views = accessLogRepository.countByOfferAndAction(
                    offer,
                    AuditAction.VIEW
            );

            LocalDateTime lastViewed =
                    accessLogRepository.findLastViewTime(offer);

            AuditAction reaction =
                    accessLogRepository
                            .findFirstByOfferAndActionInOrderByAccessedAtDesc(
                                    offer,
                                    List.of(AuditAction.ACCEPT, AuditAction.REJECT)
                            )
                            .map(OfferAccessLog::getAction)
                            .orElse(null);

            boolean isStale =
                    offer.getStatus() == OfferStatus.ODESLANA
                            && !offer.isExpired()
                            && lastViewed != null
                            && lastViewed.isBefore(staleLimit)
                            && reaction == null;

            viewsMap.put(offer.getId(), views);
            lastViewedMap.put(offer.getId(), lastViewed);
            reactionMap.put(offer.getId(), reaction);
            staleMap.put(offer.getId(), isStale);
            expiredMap.put(offer.getId(), offer.isExpired());
        }

        model.addAttribute("activeOffers", activeOffers);
        model.addAttribute("archivedOffers", archivedOffers);
        model.addAttribute("viewsMap", viewsMap);
        model.addAttribute("lastViewedMap", lastViewedMap);
        model.addAttribute("reactionMap", reactionMap);
        model.addAttribute("staleMap", staleMap);
        model.addAttribute("expiredMap", expiredMap);

        return "admin/dashboard";
    }

    // ======================================================
    // 📜 AUDIT DETAIL
    // ======================================================
    @GetMapping("/offers/{id}/audit")
    public String audit(@PathVariable Long id, Model model) {

        Offer offer = offerRepository.findById(id).orElseThrow();

        List<OfferAccessLog> logs =
                accessLogRepository.findByOfferOrderByAccessedAtDesc(offer);

        model.addAttribute("offer", offer);
        model.addAttribute("logs", logs);
        model.addAttribute("auditLabel", auditActionLabelService);

        return "admin/audit";
    }

    // ======================================================
    // REMINDERY
    // ======================================================
    @PostMapping("/offers/{id}/reminder/7")
    public String sendReminder7(@PathVariable Long id) {
        Offer offer = offerRepository.findById(id).orElseThrow();
        if (canSendReminder(offer)) {
            emailService.sendCustomerReminderSafe(offer, 7);
            offer.setFirstReminderSentAt(LocalDateTime.now());
            offerRepository.save(offer);
        }
        return "redirect:/admin";
    }

    @PostMapping("/offers/{id}/reminder/14")
    public String sendReminder14(@PathVariable Long id) {
        Offer offer = offerRepository.findById(id).orElseThrow();
        if (canSendReminder(offer)) {
            emailService.sendCustomerReminderSafe(offer, 14);
            offer.setSecondReminderSentAt(LocalDateTime.now());
            offerRepository.save(offer);
        }
        return "redirect:/admin";
    }

    private boolean canSendReminder(Offer offer) {
        return offer.getStatus() == OfferStatus.ODESLANA
                && !offer.isArchived()
                && !offer.isExpired();
    }

    // ======================================================
    // ARCHIVACE
    // ======================================================
    @PostMapping("/offers/{id}/archive")
    public String archive(@PathVariable Long id) {
        Offer offer = offerRepository.findById(id).orElseThrow();
        offer.setArchived(true);
        offer.setInEdit(false);
        offerRepository.save(offer);
        return "redirect:/admin";
    }

    @PostMapping("/offers/{id}/restore")
    public String restore(@PathVariable Long id) {
        Offer offer = offerRepository.findById(id).orElseThrow();
        offer.setArchived(false);
        offerRepository.save(offer);
        return "redirect:/admin";
    }
}