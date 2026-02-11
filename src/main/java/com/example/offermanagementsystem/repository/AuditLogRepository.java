package com.example.offermanagementsystem.repository;

import com.example.offermanagementsystem.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // =====================================================
    // DETAIL NABÍDKY (FETCH USER – řeší LazyException)
    // =====================================================
    @Query("""
    select a
    from AuditLog a
    left join fetch a.performedBy
    where a.offer.id = :offerId
    order by a.performedAt desc
""")
List<AuditLog> findAllByOfferIdWithUser(@Param("offerId") Long offerId);


    // =====================================================
    // PAGINACE
    // =====================================================
    Page<AuditLog> findByOfferIdOrderByPerformedAtDesc(
            Long offerId,
            Pageable pageable
    );

    // =====================================================
    // PODLE UŽIVATELE
    // =====================================================
    Page<AuditLog> findByPerformedByOrderByPerformedAtDesc(
            User user,
            Pageable pageable
    );

    // =====================================================
    // PODLE SEKCE
    // =====================================================
    Page<AuditLog> findBySectionOrderByPerformedAtDesc(
            AuditSection section,
            Pageable pageable
    );

    // =====================================================
    // PODLE AKCE
    // =====================================================
    Page<AuditLog> findByActionOrderByPerformedAtDesc(
            AuditAction action,
            Pageable pageable
    );

    // =====================================================
    // KOMBINACE
    // =====================================================
    Page<AuditLog> findBySectionAndActionOrderByPerformedAtDesc(
            AuditSection section,
            AuditAction action,
            Pageable pageable
    );

    // =====================================================
    // GLOBÁLNÍ
    // =====================================================
    Page<AuditLog> findAllByOrderByPerformedAtDesc(Pageable pageable);
}