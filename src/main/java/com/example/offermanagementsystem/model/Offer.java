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
    private String customerName;

    @NotBlank
    @Email
    private String customerEmail;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String description;

    // ================= CENA =================

    @NotNull
    @Column(precision = 10, scale = 2)
    private BigDecimal totalPrice;

    // ================= STAV =================

    @Enumerated(EnumType.STRING)
    private OfferStatus status = OfferStatus.NOVA;

    private Integer revision = 1;
    private boolean inEdit = false;
    private boolean archived = false;

    // ================= TOKEN =================

    @Column(nullable = false, unique = true, length = 64)
    private String customerToken = UUID.randomUUID().toString();

    @Column(nullable = false)
    private LocalDateTime tokenExpiresAt;

    // ================= AUDIT =================

    private LocalDateTime createdAt = LocalDateTime.now();

    // ================= VLASTNÍK =================

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    // ================= HELPERS =================

    public boolean isTokenValid() {
        return LocalDateTime.now().isBefore(tokenExpiresAt);
    }

    // ================= GETTERS / SETTERS =================

    public Long getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getDescription() { return description; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public OfferStatus getStatus() { return status; }
    public Integer getRevision() { return revision; }
    public boolean isInEdit() { return inEdit; }
    public boolean isArchived() { return archived; }
    public String getCustomerToken() { return customerToken; }
    public LocalDateTime getTokenExpiresAt() { return tokenExpiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public User getUser() { return user; }

    public void setCustomerName(String v) { this.customerName = v; }
    public void setCustomerEmail(String v) { this.customerEmail = v; }
    public void setDescription(String v) { this.description = v; }
    public void setTotalPrice(BigDecimal v) { this.totalPrice = v; }
    public void setStatus(OfferStatus v) { this.status = v; }
    public void setRevision(Integer v) { this.revision = v; }
    public void setInEdit(boolean v) { this.inEdit = v; }
    public void setArchived(boolean v) { this.archived = v; }
    public void setUser(User v) { this.user = v; }
    public void setTokenExpiresAt(LocalDateTime v) { this.tokenExpiresAt = v; }
}
