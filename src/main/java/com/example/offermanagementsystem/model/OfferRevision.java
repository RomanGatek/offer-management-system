package com.example.offermanagementsystem.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "offer_revisions")
public class OfferRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @Column(name = "revision_number", nullable = false)
    private int revisionNumber;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(nullable = false)
    private String description;

    @Column(name = "total_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    // ===============================
    // GETTERS
    // ===============================

    public Long getId() {
        return id;
    }

    public Offer getOffer() {
        return offer;
    }

    public int getRevisionNumber() {
        return revisionNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    // ===============================
    // SETTERS
    // ===============================

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public void setRevisionNumber(int revisionNumber) {
        this.revisionNumber = revisionNumber;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
}
