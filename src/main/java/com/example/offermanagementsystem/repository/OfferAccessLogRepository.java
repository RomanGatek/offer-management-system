package com.example.offermanagementsystem.repository;

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
    // POČET OTEVŘENÍ (VIEW)
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
    // POSLEDNÍ REAKCE
    // ===============================
    Optional<OfferAccessLog>
    findFirstByOfferAndActionInOrderByAccessedAtDesc(
            Offer offer,
            List<String> actions
    );

    // ===============================
    // 📊 STATISTIKY – KROK C
    // ===============================
    @Query("""
        select count(distinct l.offer.id)
        from OfferAccessLog l
        where l.action = 'VIEW'
          and l.offer.status = 'ODESLANA'
    """)
    long countOpenedOffers();

    @Query("""
        select count(distinct l.offer.id)
        from OfferAccessLog l
        where l.action = 'ACCEPT'
          and l.offer.status = 'ODESLANA'
    """)
    long countAcceptedOffers();
}