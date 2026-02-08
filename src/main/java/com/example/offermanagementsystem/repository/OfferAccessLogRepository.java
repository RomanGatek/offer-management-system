package com.example.offermanagementsystem.repository;

import com.example.offermanagementsystem.model.Offer;
import com.example.offermanagementsystem.model.OfferAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OfferAccessLogRepository
        extends JpaRepository<OfferAccessLog, Long> {

    // ===============================
    // POČET OTEVŘENÍ
    // ===============================
    long countByOfferAndAction(Offer offer, String action);

    // ===============================
    // POSLEDNÍ OTEVŘENÍ (VIEW)
    // ===============================
    @Query("""
        select max(l.accessedAt)
        from OfferAccessLog l
        where l.offer = :offer
          and l.action = 'VIEW'
    """)
    LocalDateTime findLastViewTime(Offer offer);

    // ===============================
    // EXISTUJE REAKCE?
    // ===============================
    boolean existsByOfferAndActionIn(
            Offer offer,
            Iterable<String> actions
    );

    // ===============================
    // POSLEDNÍ REAKCE ZÁKAZNÍKA
    // (Spring Data naming → LIMIT 1 OK)
    // ===============================
    Optional<OfferAccessLog>
    findFirstByOfferAndActionInOrderByAccessedAtDesc(
            Offer offer,
            Iterable<String> actions
    );
}