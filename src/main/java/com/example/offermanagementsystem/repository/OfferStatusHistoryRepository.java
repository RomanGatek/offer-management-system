package com.example.offermanagementsystem.repository;

import com.example.offermanagementsystem.model.Offer;
import com.example.offermanagementsystem.model.OfferStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferStatusHistoryRepository
        extends JpaRepository<OfferStatusHistory, Long> {

    List<OfferStatusHistory> findByOfferOrderByChangedAtDesc(Offer offer);
}