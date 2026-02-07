package com.example.offermanagementsystem.repository;

import com.example.offermanagementsystem.model.Offer;
import com.example.offermanagementsystem.model.OfferRevision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferRevisionRepository extends JpaRepository<OfferRevision, Long> {
    List<OfferRevision> findByOfferOrderByRevisionNumberDesc(Offer offer);
}
