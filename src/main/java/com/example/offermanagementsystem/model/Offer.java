package com.example.offermanagementsystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "offers")
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Zákazník je povinný")
    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @NotBlank(message = "Email zákazníka je povinný")
    @Email(message = "Neplatný email")
    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @NotBlank(message = "Popis je povinný")
    @Column(nullable = false)
    private String description;

    @NotNull(message = "Cena je povinná")
    @DecimalMin(value = "0.01", message = "Cena musí být větší než 0")
    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status = OfferStatus.NOVA;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ===== GETTERY / SETTERY =====

    public Long getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getDescription() { return description; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public LocalDate getCreatedDate() { return createdDate; }
    public OfferStatus getStatus() { return status; }
    public User getUser() { return user; }

    public void setId(Long id) { this.id = id; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public void setDescription(String description) { this.description = description; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
    public void setStatus(OfferStatus status) { this.status = status; }
    public void setUser(User user) { this.user = user; }
}