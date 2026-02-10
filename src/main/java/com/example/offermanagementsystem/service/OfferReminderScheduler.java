package com.example.offermanagementsystem.service;

import com.example.offermanagementsystem.model.Offer;
import com.example.offermanagementsystem.repository.OfferRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OfferReminderScheduler {

    private static final int FIRST_REMINDER_DAYS = 7;
    private static final int SECOND_REMINDER_DAYS = 14;

    private final OfferRepository offerRepository;
    private final EmailService emailService;

    public OfferReminderScheduler(
            OfferRepository offerRepository,
            EmailService emailService
    ) {
        this.offerRepository = offerRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 8 * * *") // každý den v 8 ráno
    public void processReminders() {

        List<Offer> offers = offerRepository.findByArchivedFalse();

        for (Offer offer : offers) {

            if (offer.needsFirstReminder(FIRST_REMINDER_DAYS)) {
                emailService.sendCustomerReminderSafe(offer, 7);
                offer.setFirstReminderSentAt(LocalDateTime.now());
                offerRepository.save(offer);
            }

            if (offer.needsSecondReminder(SECOND_REMINDER_DAYS)) {
                emailService.sendCustomerReminderSafe(offer, 14);
                offer.setSecondReminderSentAt(LocalDateTime.now());
                offerRepository.save(offer);
            }
        }
    }
}
