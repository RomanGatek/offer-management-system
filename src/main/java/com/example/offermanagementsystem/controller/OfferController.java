package com.example.offermanagementsystem.controller;

import com.example.offermanagementsystem.model.Offer;
import com.example.offermanagementsystem.model.OfferStatus;
import com.example.offermanagementsystem.model.User;
import com.example.offermanagementsystem.repository.OfferRepository;
import com.example.offermanagementsystem.repository.UserRepository;
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
import java.util.List;

@Controller
@RequestMapping("/offers")
public class OfferController {

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PdfExportService pdfExportService;

    // Zobrazí seznam nabídek (admin vše, ostatní jen své)
    @GetMapping
    public String listOffers(Model model, Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        List<Offer> offers = user.getRole().equals("ADMIN")
                ? offerRepository.findAll()
                : offerRepository.findByUser(user);

        model.addAttribute("offers", offers);
        return "offers/list";
    }

    // Formulář pro novou nabídku
    @GetMapping("/new")
    public String newOfferForm(Model model) {
        Offer offer = new Offer();
        offer.setStatus(OfferStatus.NOVA); // nastavíme výchozí hodnotu
        model.addAttribute("offer", offer);
        return "offers/new";
    }

    // Uloží novou nabídku
    @PostMapping
    public String createOffer(@Valid @ModelAttribute Offer offer, BindingResult result, Principal principal) {
        System.out.println("Offer status received: " + offer.getStatus());  // ← ZDE

        if (result.hasErrors()) {
            return "offers/new";
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        offer.setUser(user);

        if (offer.getStatus() == null) {
            offer.setStatus(OfferStatus.NOVA);
        }

        offerRepository.save(offer);
        System.out.println(">>> STATUS při odeslání: " + offer.getStatus());
        return "redirect:/offers";

    }

    // Formulář pro editaci nabídky
    @GetMapping("/edit/{id}")
    public String editOfferForm(@PathVariable Long id, Model model, Principal principal) {
        Offer offer = offerRepository.findById(id).orElseThrow();
        User currentUser = getCurrentUser(principal);

        if (!canEditOffer(currentUser, offer)) {
            return "redirect:/offers?error=unauthorized";
        }

        model.addAttribute("offer", offer);
        return "offers/edit";
    }

    // Uloží změny nabídky
    @PostMapping("/update/{id}")
    public String updateOffer(@PathVariable Long id,
                              @Valid @ModelAttribute Offer updatedOffer,
                              BindingResult result,
                              Principal principal,
                              Model model) {
        Offer offer = offerRepository.findById(id).orElseThrow();
        User currentUser = getCurrentUser(principal);

        if (!canEditOffer(currentUser, offer)) {
            return "redirect:/offers?error=unauthorized";
        }

        if (result.hasErrors()) {
            model.addAttribute("offer", updatedOffer);
            return "offers/edit";
        }

        // Základní pole může upravit každý
        offer.setCustomerName(updatedOffer.getCustomerName());
        offer.setDescription(updatedOffer.getDescription());
        offer.setTotalPrice(updatedOffer.getTotalPrice());

        // Status pouze admin
        if ("ADMIN".equals(currentUser.getRole())) {
            offer.setStatus(updatedOffer.getStatus());
        }

        offerRepository.save(offer);
        return "redirect:/offers";
    }

    // Smazání nabídky
    @GetMapping("/delete/{id}")
    public String deleteOffer(@PathVariable Long id, Principal principal) {
        Offer offer = offerRepository.findById(id).orElseThrow();
        User currentUser = getCurrentUser(principal);

        if (!canEditOffer(currentUser, offer)) {
            return "redirect:/offers?error=unauthorized";
        }

        offerRepository.delete(offer);
        return "redirect:/offers";
    }

    // Export nabídky do PDF
    @GetMapping("/export/{id}")
    public void exportOfferToPdf(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Offer offer = offerRepository.findById(id).orElseThrow();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=nabidka-" + id + ".pdf");

        ByteArrayInputStream pdfStream = pdfExportService.generateOfferPdf(offer);
        pdfStream.transferTo(response.getOutputStream());
    }

    // Helper metody

    private User getCurrentUser(Principal principal) {
        String username = principal.getName();
        return userRepository.findByUsername(username).orElseThrow();
    }

    private boolean canEditOffer(User user, Offer offer) {
        return user.getRole().equals("ADMIN") || offer.getUser().equals(user);
    }
}