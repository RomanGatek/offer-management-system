package com.example.offermanagementsystem.service;

import com.example.offermanagementsystem.model.*;
import com.example.offermanagementsystem.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void log(
            Offer offer,
            AuditAction action,
            AuditSection section,
            User user,
            String detail,
            String oldValue,
            String newValue,
            HttpServletRequest request
    ) {

        AuditLog log = new AuditLog();
        log.setOffer(offer);
        log.setAction(action);
        log.setSection(section);
        log.setPerformedBy(user);
        log.setDetail(detail);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setPerformedAt(LocalDateTime.now());

        if (request != null) {
            log.setIpAddress(request.getRemoteAddr());
            log.setUserAgent(request.getHeader("User-Agent"));
        }

        repository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getOfferAudit(Long offerId) {
        return repository.findAllByOfferIdWithUser(offerId);
    }
}