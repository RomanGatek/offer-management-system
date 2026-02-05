package com.example.offermanagementsystem.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "offer_status_history")
public class OfferStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== VAZBA NA NABÍDKU =====
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    // ===== STAVY =====
    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private OfferStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private OfferStatus toStatus;

    // ===== KDO ZMĚNIL =====
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    // ===== KDY =====
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    // ================= GETTERY =================
    public Long getId() {
        return id;
    }

    public Offer getOffer() {
        return offer;
    }

    public OfferStatus getFromStatus() {
        return fromStatus;
    }

    public OfferStatus getToStatus() {
        return toStatus;
    }

    public User getChangedBy() {
        return changedBy;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    // ================= SETTERY =================
    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public void setFromStatus(OfferStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public void setToStatus(OfferStatus toStatus) {
        this.toStatus = toStatus;
    }

    public void setChangedBy(User changedBy) {
        this.changedBy = changedBy;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}