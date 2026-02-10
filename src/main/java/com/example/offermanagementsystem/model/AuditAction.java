package com.example.offermanagementsystem.model;

public enum AuditAction {

    // ===== NABÍDKA – ADMIN =====
    OFFER_CREATED,
    OFFER_UPDATED,
    OFFER_STATUS_CHANGED,
    OFFER_EDIT_LOCKED,
    OFFER_EDIT_UNLOCKED,
    OFFER_DELETED,
    OFFER_ARCHIVED,
    PDF_EXPORTED,

    // ===== EMAILY =====
    EMAIL_SENT,
    EMAIL_OPENED,
    REMINDER_7,
    REMINDER_14,

    // ===== VEŘEJNÁ ČÁST =====
    VIEW,
    ACCEPT,
    REJECT,

    // ===== SYSTÉM =====
    EXPIRED;

    // ======================================================
    // P2 – KATEGORIE AUDITU
    // ======================================================
    public AuditSection section() {

        if (this.name().startsWith("OFFER") || this == PDF_EXPORTED) {
            return AuditSection.ADMIN;
        }

        if (this.name().startsWith("EMAIL")
                || this.name().startsWith("REMINDER")
                || this == EXPIRED) {
            return AuditSection.SYSTEM;
        }

        if (this == VIEW || this == ACCEPT || this == REJECT) {
            return AuditSection.CUSTOMER;
        }

        return AuditSection.SYSTEM;
    }
}