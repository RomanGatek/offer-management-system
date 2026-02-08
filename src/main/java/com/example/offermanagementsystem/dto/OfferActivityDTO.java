package com.example.offermanagementsystem.dto;

import java.time.LocalDateTime;

public class OfferActivityDTO {

    private long views;
    private LocalDateTime lastViewedAt;
    private String reaction; // ACCEPT / REJECT / null

    public OfferActivityDTO(
            long views,
            LocalDateTime lastViewedAt,
            String reaction
    ) {
        this.views = views;
        this.lastViewedAt = lastViewedAt;
        this.reaction = reaction;
    }

    public long getViews() {
        return views;
    }

    public LocalDateTime getLastViewedAt() {
        return lastViewedAt;
    }

    public String getReaction() {
        return reaction;
    }

    // helpery pro Thymeleaf
    public boolean neverOpened() {
        return views == 0;
    }

    public boolean waitingForReaction() {
        return views > 0 && reaction == null;
    }

    public boolean accepted() {
        return "ACCEPT".equals(reaction);
    }

    public boolean rejected() {
        return "REJECT".equals(reaction);
    }
}