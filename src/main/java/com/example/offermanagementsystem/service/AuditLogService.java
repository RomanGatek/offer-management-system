package com.example.offermanagementsystem.service;

import com.example.offermanagementsystem.model.*;
import com.example.offermanagementsystem.repository.OfferAccessLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditLogService {

    private final OfferAccessLogRepository accessLogRepository;

    public AuditLogService(OfferAccessLogRepository accessLogRepository) {
        this.accessLogRepository = accessLogRepository;
    }

    public void log(Offer offer, AuditAction action) {
        OfferAccessLog log = new OfferAccessLog(
                offer,
                action,
                null,
                null
        );
        log.setAccessedAt(LocalDateTime.now());
        accessLogRepository.save(log);
    }

    public void log(Offer offer, AuditAction action, HttpServletRequest request) {
        OfferAccessLog log = new OfferAccessLog(
                offer,
                action,
                request != null ? request.getRemoteAddr() : null,
                request != null ? request.getHeader("User-Agent") : null
        );
        log.setAccessedAt(LocalDateTime.now());
        accessLogRepository.save(log);
    }
}
