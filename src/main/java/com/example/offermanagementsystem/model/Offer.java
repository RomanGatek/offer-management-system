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

    // ================= REMINDERY =================

    /** první reminder (7 dní) */
    private LocalDateTime firstReminderSentAt;

    /** druhý reminder (14 dní) */
    private LocalDateTime secondReminderSentAt;

    // ================= EXPIRACE =================

    private boolean expired = false;

    private LocalDateTime expiredAt;


    // ================= AUDIT =================

    private LocalDateTime createdAt = LocalDateTime.now();

    // ================= VLASTNÍK =================

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    // ================= HELPERS =================

    public boolean isTokenValid() {
        return LocalDateTime.now().isBefore(tokenExpiresAt);
    }

    /** B7.1 – první reminder */
    public boolean needsFirstReminder(int days) {
        return status == OfferStatus.ODESLANA
                && !archived
                && !expired
                && firstReminderSentAt == null
                && createdAt.isBefore(LocalDateTime.now().minusDays(days));
    }

    /** B7.2 – druhý reminder */
    public boolean needsSecondReminder(int days) {
        return status == OfferStatus.ODESLANA
                && !archived
                && !expired
                && firstReminderSentAt != null
                && secondReminderSentAt == null
                && createdAt.isBefore(LocalDateTime.now().minusDays(days));
    }

    /** B8 – expirace nabídky */
    public boolean shouldExpire(int days) {
        return status == OfferStatus.ODESLANA
                && !archived
                && !expired
                && createdAt.isBefore(LocalDateTime.now().minusDays(days));
    }

    // ================= GETTERS =================

    public Long getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getDescription() { return description; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public OfferStatus getStatus() { return status; }
    public Integer getRevision() { return revision; }
    public boolean isInEdit() { return inEdit; }
    public boolean isArchived() { return archived; }
    public boolean isExpired() { return expired; }
    public LocalDateTime getExpiredAt() { return expiredAt; }
    public String getCustomerToken() { return customerToken; }
    public LocalDateTime getTokenExpiresAt() { return tokenExpiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getFirstReminderSentAt() { return firstReminderSentAt; }
    public LocalDateTime getSecondReminderSentAt() { return secondReminderSentAt; }
    public User getUser() { return user; }

    // ================= SETTERS =================

    public void setCustomerName(String v) { this.customerName = v; }
    public void setCustomerEmail(String v) { this.customerEmail = v; }
    public void setDescription(String v) { this.description = v; }
    public void setTotalPrice(BigDecimal v) { this.totalPrice = v; }
    public void setStatus(OfferStatus v) { this.status = v; }
    public void setRevision(Integer v) { this.revision = v; }
    public void setInEdit(boolean v) { this.inEdit = v; }
    public void setArchived(boolean v) { this.archived = v; }
    public void setExpired(boolean v) { this.expired = v; }
    public void setExpiredAt(LocalDateTime v) { this.expiredAt = v; }
    public void setUser(User v) { this.user = v; }
    public void setTokenExpiresAt(LocalDateTime v) { this.tokenExpiresAt = v; }
    public void setFirstReminderSentAt(LocalDateTime v) { this.firstReminderSentAt = v; }
    public void setSecondReminderSentAt(LocalDateTime v) { this.secondReminderSentAt = v; }
}
