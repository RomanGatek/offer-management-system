package com.example.offermanagementsystem.repository;

import com.example.offermanagementsystem.model.Offer;
import com.example.offermanagementsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    // 🔹 nabídky obchodníka
    List<Offer> findByUser(User user);

    // 🔹 zákaznický přístup přes token
    Optional<Offer> findByCustomerToken(String customerToken);
}