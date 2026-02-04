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
    private String customerName;

    @NotBlank(message = "Popis je povinný")
    private String description;

    @NotNull(message = "Cena je povinná")
    @DecimalMin(value = "0.01", message = "Cena musí být větší než 0")
    private BigDecimal totalPrice;

    private LocalDate createdDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private OfferStatus status = OfferStatus.NOVA;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Offer() {}

    public Offer(String customerName, String description, BigDecimal totalPrice, User user) {
        this.customerName = customerName;
        this.description = description;
        this.totalPrice = totalPrice;
        this.user = user;
        this.createdDate = LocalDate.now();
        this.status = OfferStatus.NOVA;
    }

    // Gettery
    public Long getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getDescription() { return description; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public LocalDate getCreatedDate() { return createdDate; }
    public OfferStatus getStatus() { return status; }
    public User getUser() { return user; }

    // Settery
    public void setId(Long id) { this.id = id; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setDescription(String description) { this.description = description; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
    public void setStatus(OfferStatus status) { this.status = status; }
    public void setUser(User user) { this.user = user; }
}
