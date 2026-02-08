package com.example.offermanagementsystem.controller;

import com.example.offermanagementsystem.model.Offer;
import com.example.offermanagementsystem.model.User;
import com.example.offermanagementsystem.repository.OfferRepository;
import com.example.offermanagementsystem.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final OfferRepository offerRepository;
    private final UserRepository userRepository;

    public DashboardController(OfferRepository offerRepository,
                               UserRepository userRepository) {
        this.offerRepository = offerRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String dashboard(Authentication authentication, Model model) {

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        List<Offer> offers;

        if (isAdmin(user)) {
            // ✅ ADMIN – všechny aktivní nabídky
            offers = offerRepository.findByArchivedFalse();
        } else {
            // ✅ USER – jen své aktivní nabídky
            offers = offerRepository.findByUserAndArchivedFalse(user);
        }

        model.addAttribute("offers", offers);
        model.addAttribute("user", user);

        return "dashboard";
    }

    private boolean isAdmin(User user) {
        return "ROLE_ADMIN".equals(user.getRole()) || "ADMIN".equals(user.getRole());
    }
}