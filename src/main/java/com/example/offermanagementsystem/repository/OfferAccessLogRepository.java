package com.example.offermanagementsystem.repository;

import com.example.offermanagementsystem.model.AuditAction;
import com.example.offermanagementsystem.model.Offer;
import com.example.offermanagementsystem.model.OfferAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OfferAccessLogRepository
        extends JpaRepository<OfferAccessLog, Long> {

    // ===============================
    // POČET AKCÍ
    // ===============================
    long countByOfferAndAction(Offer offer, AuditAction action);

    // ===============================
    // EXISTUJE AKCE?
    // ===============================
    boolean existsByOfferAndAction(Offer offer, AuditAction action);

    // ===============================
    // POSLEDNÍ VIEW (PUBLIC)
    // ===============================
    @Query("""
        select max(l.accessedAt)
        from OfferAccessLog l
        where l.offer = :offer
          and l.action = com.example.offermanagementsystem.model.AuditAction.VIEW
    """)
    LocalDateTime findLastViewTime(Offer offer);

    // ===============================
    // POSLEDNÍ REAKCE (ACCEPT / REJECT)
    // ===============================
    Optional<OfferAccessLog>
    findFirstByOfferAndActionInOrderByAccessedAtDesc(
            Offer offer,
            Iterable<AuditAction> actions
    );

    // ===============================
    // 📜 AUDIT – ADMIN (nejnovější nahoře)
    // ===============================
    List<OfferAccessLog> findByOfferOrderByAccessedAtDesc(Offer offer);

    // ===============================
    // 📜 AUDIT – ZÁKAZNÍK (timeline)
    // ===============================
    List<OfferAccessLog> findByOfferOrderByAccessedAtAsc(Offer offer);
}