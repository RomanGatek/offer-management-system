package com.example.offermanagementsystem.scheduler;

import com.example.offermanagementsystem.service.OfferReminderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OfferReminderJob {

    private final OfferReminderService reminderService;

    public OfferReminderJob(OfferReminderService reminderService) {
        this.reminderService = reminderService;
    }

    // 1× denně v 08:00
    @Scheduled(cron = "0 0 8 * * *")
    public void runDaily() {
        reminderService.processReminders();
    }
}