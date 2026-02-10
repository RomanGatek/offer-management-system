package com.example.offermanagementsystem.controller;

import com.example.offermanagementsystem.model.Offer;
import com.example.offermanagementsystem.model.OfferAccessLog;
import com.example.offermanagementsystem.repository.OfferAccessLogRepository;
import com.example.offermanagementsystem.repository.OfferRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/public/offers")
public class PublicAuditController {

    private final OfferRepository offerRepository;
    private final OfferAccessLogRepository accessLogRepository;

    // Jen akce, které dává smysl ukazovat zákazníkovi
    private static final Set<String> CUSTOMER_VISIBLE_ACTIONS = Set.of(
            "EMAIL_SENT",
            "EMAIL_OPENED",
            "REMINDER_7",
            "REMINDER_14",
            "VIEW",        // volitelně: můžeš přejmenovat na "Otevřeno"
            "ACCEPT",
            "REJECT",
            "EXPIRED"
    );

    public PublicAuditController(
            OfferRepository offerRepository,
            OfferAccessLogRepository accessLogRepository
    ) {
        this.offerRepository = offerRepository;
        this.accessLogRepository = accessLogRepository;
    }

    @GetMapping("/{token}/audit")
    public String customerAudit(@PathVariable String token, Model model) {

        Offer offer = offerRepository.findByCustomerToken(token).orElseThrow();

        List<OfferAccessLog> allLogs =
                accessLogRepository.findByOfferOrderByAccessedAtAsc(offer);

        // Filtrujeme jen “bezpečné” akce pro zákazníka
        List<OfferAccessLog> logs = allLogs.stream()
                .filter(l -> l.getAction() != null && CUSTOMER_VISIBLE_ACTIONS.contains(l.getAction()))
                .toList();

        model.addAttribute("offer", offer);
        model.addAttribute("logs", logs);
        model.addAttribute("publicUrl", "/public/offers/" + token);

        return "public/audit";
    }

    // ======================================================
    // Helper pro hezké názvy v UI (můžeš přesunout do utilu později)
    // ======================================================
    @ModelAttribute("actionLabel")
    public java.util.function.Function<String, String> actionLabel() {
        return action -> switch (action) {
            case "EMAIL_SENT" -> "📤 Email odeslán";
            case "EMAIL_OPENED" -> "👀 Email otevřen";
            case "REMINDER_7" -> "🕒 Připomenutí po 7 dnech";
            case "REMINDER_14" -> "⏰ Připomenutí po 14 dnech";
            case "VIEW" -> "📄 Nabídka otevřena";
            case "ACCEPT" -> "✅ Nabídka přijata";
            case "REJECT" -> "❌ Nabídka zamítnuta";
            case "EXPIRED" -> "⛔ Nabídka expirovala";
            default -> action;
        };
    }
}