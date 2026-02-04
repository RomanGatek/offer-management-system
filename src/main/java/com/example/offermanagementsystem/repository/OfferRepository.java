package com.example.offermanagementsystem.repository;

import com.example.offermanagementsystem.model.Offer;
import com.example.offermanagementsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findByUser(User user);
}