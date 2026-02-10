package com.example.offermanagementsystem.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "offer_access_log",
        indexes = {
                @Index(name = "idx_offer_action", columnList = "offer_id, action"),
                @Index(name = "idx_offer_accessed_at", columnList = "accessed_at")
        }
)
public class OfferAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==================================
    // VAZBA NA NABÍDKU
    // ==================================
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    // ==================================
    // KDY
    // ==================================
    @Column(name = "accessed_at", nullable = false)
    private LocalDateTime accessedAt;

    // ==================================
    // KDO / ODKUD
    // ==================================
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    // ==================================
    // CO SE STALO (AUDIT)
    // ==================================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction action;

    // ==================================
    // KONSTRUKTORY
    // ==================================
    public OfferAccessLog() {
    }

    public OfferAccessLog(
            Offer offer,
            AuditAction action,
            String ipAddress,
            String userAgent
    ) {
        this.offer = offer;
        this.action = action;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.accessedAt = LocalDateTime.now();
    }

    // ==================================
    // GETTERY / SETTERY
    // ==================================
    public Long getId() {
        return id;
    }

    public Offer getOffer() {
        return offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public LocalDateTime getAccessedAt() {
        return accessedAt;
    }

    public void setAccessedAt(LocalDateTime accessedAt) {
        this.accessedAt = accessedAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }
}