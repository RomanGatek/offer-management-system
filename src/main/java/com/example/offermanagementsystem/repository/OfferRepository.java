package com.example.offermanagementsystem.repository;

import com.example.offermanagementsystem.model.Offer;
import com.example.offermanagementsystem.model.User;
import com.example.offermanagementsystem.model.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    // =========================
    // USER DASHBOARD
    // =========================

    // pouze aktivní nabídky uživatele
    List<Offer> findByUserAndArchivedFalse(User user);

    // =========================
    // ADMIN DASHBOARD
    // =========================

    // aktivní nabídky
    List<Offer> findByArchivedFalse();

    // archivované nabídky
    List<Offer> findByArchivedTrue();

    // =========================
    // WORKFLOW / STAVY
    // =========================

    List<Offer> findByStatus(OfferStatus status);

    List<Offer> findByUserAndStatus(User user, OfferStatus status);

    // =========================
    // ZÁKAZNICKÝ TOKEN
    // =========================

    Optional<Offer> findByCustomerToken(String customerToken);

    // =========================
    // STATISTIKY
    // =========================

    long countByStatus(OfferStatus status);

    long countByArchivedFalse();

    long countByArchivedTrue();

    long countByUser(User user);
}
