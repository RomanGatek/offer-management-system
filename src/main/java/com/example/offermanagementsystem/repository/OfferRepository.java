package com.example.offermanagementsystem.repository;

import com.example.offermanagementsystem.model.AuditAction;
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
    // USER / ADMIN
    // =========================
    List<Offer> findByUserAndArchivedFalse(User user);

    List<Offer> findByArchivedFalse();

    List<Offer> findByArchivedTrue();

    // 👉 KROK J – SKRÝT EXPIROVANÉ
    List<Offer> findByArchivedFalseAndExpiredFalse();

    // 👉 Q – SCHEDULER (EXPIRACE)
    List<Offer> findByExpiredFalseAndArchivedFalse();

    // =========================
    // WORKFLOW
    // =========================
    List<Offer> findByStatus(OfferStatus status);

    Optional<Offer> findByCustomerToken(String customerToken);

    // =========================
    // REMINDER 7 DNÍ
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
    // STATISTIKY (KROK C)
    // =========================
    long countByStatus(OfferStatus status);

    @Query("""
        select count(distinct l.offer.id)
        from OfferAccessLog l
        where l.action = com.example.offermanagementsystem.model.AuditAction.EMAIL_OPENED
    """)
    long countOpenedOffers();

    @Query("""
        select count(distinct l.offer.id)
        from OfferAccessLog l
        where l.action = com.example.offermanagementsystem.model.AuditAction.ACCEPT
    """)
    long countAcceptedOffers();
}