package com.example.offermanagementsystem.repository;

import com.example.offermanagementsystem.model.Offer;
import com.example.offermanagementsystem.model.OfferStatus;
import com.example.offermanagementsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    // =========================
    // USER DASHBOARD
    // =========================
    List<Offer> findByUserAndArchivedFalse(User user);

    // =========================
    // ADMIN DASHBOARD
    // =========================
    List<Offer> findByArchivedFalse();
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
    // B6/B7 – REMINDER (7 DNÍ)
    // =========================
    @Query("""
        select o
        from Offer o
        where o.archived = false
          and o.status = com.example.offermanagementsystem.model.OfferStatus.ODESLANA
          and o.firstReminderSentAt is null
          and o.createdAt < :limit
    """)
    List<Offer> findOffersForFirstReminder(LocalDateTime limit);

    // =========================
    // STATISTIKY – KROK C
    // =========================
    long countByStatus(OfferStatus status);

    @Query("""
        select count(distinct o)
        from Offer o
        join OfferAccessLog l on l.offer = o
        where l.action = 'VIEW'
    """)
    long countOpenedOffers();

    @Query("""
        select count(o)
        from Offer o
        where o.status = com.example.offermanagementsystem.model.OfferStatus.PRIJATA
    """)
    long countAcceptedOffers();

    long countByArchivedFalse();
    long countByArchivedTrue();
    long countByUser(User user);
}