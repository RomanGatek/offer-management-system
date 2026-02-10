package com.example.offermanagementsystem.service;

import com.example.offermanagementsystem.model.Offer;
import com.example.offermanagementsystem.model.OfferStatus;
import com.example.offermanagementsystem.repository.OfferRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OfferExpirationScheduler {

    private static final int EXPIRE_AFTER_DAYS = 30;

    private final OfferRepository offerRepository;
    private final EmailService emailService;

    public OfferExpirationScheduler(
            OfferRepository offerRepository,
            EmailService emailService
    ) {
        this.offerRepository = offerRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 30 8 * * *")
    public void expireOffers() {

        List<Offer> offers = offerRepository.findByArchivedFalse();

        for (Offer offer : offers) {
            if (offer.shouldExpire(EXPIRE_AFTER_DAYS)) {
                offer.setExpired(true);
                offer.setStatus(OfferStatus.EXPIROVANA);
                offerRepository.save(offer);
                emailService.sendExpirationEmailSafe(offer);
            }
        }
    }
}
