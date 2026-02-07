package com.example.offermanagementsystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "offers")
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= ZÁKAZNÍK =================

    @NotBlank
    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @NotBlank
    @Email
    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    // ================= CENA =================

    @NotNull
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    // ================= STAV =================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status = OfferStatus.NOVA;

    /** číslo revize (od 1) */
    @Column(nullable = false)
    private Integer revision = 1;

    /** lock při editaci */
    @Column(name = "in_edit", nullable = false)
    private boolean inEdit = false;

    // ================= TOKEN =================

    @Column(name = "customer_token", nullable = false, unique = true, length = 64)
    private String customerToken = UUID.randomUUID().toString();

    // ================= AUDIT =================

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ================= VLASTNÍK =================

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ================= GETTERS =================

    public Long getId() {
        return id;
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

    public OfferStatus getStatus() {
        return status;
    }

    public Integer getRevision() {
        return revision;
    }

    public boolean isInEdit() {
        return inEdit;
    }

    public String getCustomerToken() {
        return customerToken;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    // ================= SETTERS =================

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

    public void setStatus(OfferStatus status) {
        this.status = status;
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
    }

    public void setInEdit(boolean inEdit) {
        this.inEdit = inEdit;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
