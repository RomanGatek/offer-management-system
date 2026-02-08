package com.example.offermanagementsystem.repository;

import com.example.offermanagementsystem.model.AuditLog;
import com.example.offermanagementsystem.model.Offer;
import com.example.offermanagementsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // audit pro jednu nabídku (nejnovější nahoře)
    List<AuditLog> findByOfferOrderByPerformedAtDesc(Offer offer);

    // audit pro uživatele (nejnovější nahoře)
    List<AuditLog> findByPerformedByOrderByPerformedAtDesc(User user);
}