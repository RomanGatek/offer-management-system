package com.example.offermanagementsystem.service;

import com.example.offermanagementsystem.model.AuditAction;
import com.example.offermanagementsystem.model.Offer;
import com.example.offermanagementsystem.model.OfferStatus;
import com.example.offermanagementsystem.repository.OfferRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OfferExpirationScheduler {

    private final OfferRepository offerRepository;
    private final EmailService emailService;

    public OfferExpirationScheduler(
            OfferRepository offerRepository,
            EmailService emailService
    ) {
        this.offerRepository = offerRepository;
        this.emailService = emailService;
    }

    // ======================================================
    // Q – AUTOMATICKÁ EXPIRACE
    // ======================================================
    @Scheduled(cron = "0 30 8 * * *") // každý den v 8:30
    public void expireOffers() {

        List<Offer> offers =
                offerRepository.findByExpiredFalseAndArchivedFalse();

        for (Offer offer : offers) {

            if (offer.getTokenExpiresAt() != null &&
                    offer.getTokenExpiresAt().isBefore(LocalDateTime.now())) {

                offer.setExpired(true);
                offer.setStatus(OfferStatus.EXPIROVANA);
                offerRepository.save(offer);

                emailService.sendExpirationEmailSafe(offer);
            }
        }
    }
}