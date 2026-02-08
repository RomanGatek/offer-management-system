package com.example.offermanagementsystem.controller;

import com.example.offermanagementsystem.model.Offer;
import com.example.offermanagementsystem.model.OfferAccessLog;
import com.example.offermanagementsystem.repository.OfferAccessLogRepository;
import com.example.offermanagementsystem.repository.OfferRepository;
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

    private final OfferRepository offerRepository;
    private final OfferAccessLogRepository accessLogRepository;

    public AdminController(
            OfferRepository offerRepository,
            OfferAccessLogRepository accessLogRepository
    ) {
        this.offerRepository = offerRepository;
        this.accessLogRepository = accessLogRepository;
    }

    // ===============================
    // ADMIN DASHBOARD
    // ===============================
    @GetMapping
    public String adminDashboard(Model model) {

        List<Offer> activeOffers = offerRepository.findByArchivedFalse();
        List<Offer> archivedOffers = offerRepository.findByArchivedTrue();

        Map<Long, Long> viewsMap = new HashMap<>();
        Map<Long, LocalDateTime> lastViewedMap = new HashMap<>();
        Map<Long, String> reactionMap = new HashMap<>();

        for (Offer offer : activeOffers) {

            // počet otevření
            long views =
                    accessLogRepository.countByOfferAndAction(offer, "VIEW");

            // poslední otevření
            LocalDateTime lastViewed =
                    accessLogRepository.findLastViewTime(offer);

            // poslední reakce
            String reaction =
                    accessLogRepository
                            .findFirstByOfferAndActionInOrderByAccessedAtDesc(
                                    offer,
                                    List.of("ACCEPT", "REJECT")
                            )
                            .map(OfferAccessLog::getAction)
                            .orElse(null);

            viewsMap.put(offer.getId(), views);
            lastViewedMap.put(offer.getId(), lastViewed);
            reactionMap.put(offer.getId(), reaction);
        }

        model.addAttribute("activeOffers", activeOffers);
        model.addAttribute("archivedOffers", archivedOffers);
        model.addAttribute("viewsMap", viewsMap);
        model.addAttribute("lastViewedMap", lastViewedMap);
        model.addAttribute("reactionMap", reactionMap);

        return "admin/dashboard";
    }

    // ===============================
    // ARCHIVE
    // ===============================
    @PostMapping("/offers/{id}/archive")
    public String archive(@PathVariable Long id) {

        Offer offer = offerRepository.findById(id).orElseThrow();
        offer.setArchived(true);
        offer.setInEdit(false);
        offerRepository.save(offer);

        return "redirect:/admin";
    }

    // ===============================
    // RESTORE
    // ===============================
    @PostMapping("/offers/{id}/restore")
    public String restore(@PathVariable Long id) {

        Offer offer = offerRepository.findById(id).orElseThrow();
        offer.setArchived(false);
        offerRepository.save(offer);

        return "redirect:/admin";
    }
}