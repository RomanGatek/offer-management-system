package com.example.offermanagementsystem.service;

import com.example.offermanagementsystem.model.Offer;
import com.example.offermanagementsystem.repository.OfferRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OfferReminderService {

    private static final int FIRST_REMINDER_DAYS = 7;
    private static final int SECOND_REMINDER_DAYS = 14;
    private static final int EXPIRATION_DAYS = 21;

    private final OfferRepository offerRepository;
    private final EmailService emailService;

    public OfferReminderService(
            OfferRepository offerRepository,
            EmailService emailService
    ) {
        this.offerRepository = offerRepository;
        this.emailService = emailService;
    }

    // ======================================================
    // ZPĚTNÁ KOMPATIBILITA – VOLÁ SCHEDULER
    // ======================================================
    public void processReminders() {
        processRemindersAndExpirations();
    }

    // ======================================================
    // HLAVNÍ JOB – REMINDERY + EXPIRACE
    // ======================================================
    public void processRemindersAndExpirations() {

        List<Offer> offers = offerRepository.findByArchivedFalse();
        LocalDateTime now = LocalDateTime.now();

        for (Offer offer : offers) {

            // ===============================
            // 1️⃣ PRVNÍ REMINDER (7 DNÍ)
            // ===============================
            if (offer.needsFirstReminder(FIRST_REMINDER_DAYS)) {

                emailService.sendCustomerReminderSafe(offer, 7);
                offer.setFirstReminderSentAt(now);
                offerRepository.save(offer);
                continue;
            }

            // ===============================
            // 2️⃣ DRUHÝ REMINDER (14 DNÍ)
            // ===============================
            if (offer.needsSecondReminder(SECOND_REMINDER_DAYS)) {

                emailService.sendCustomerReminderSafe(offer, 14);
                offer.setSecondReminderSentAt(now);
                offerRepository.save(offer);
                continue;
            }

            // ===============================
            // 3️⃣ EXPIRACE (21 DNÍ)
            // ===============================
            if (offer.shouldExpire(EXPIRATION_DAYS)) {

                offer.setExpired(true);
                offer.setExpiredAt(now);
                offerRepository.save(offer);

                emailService.sendExpirationEmailSafe(offer);
            }
        }
    }
}