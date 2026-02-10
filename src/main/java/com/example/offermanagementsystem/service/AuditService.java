package com.example.offermanagementsystem.service;

import com.example.offermanagementsystem.model.AuditAction;
import com.example.offermanagementsystem.model.Offer;
import com.example.offermanagementsystem.model.OfferAccessLog;
import com.example.offermanagementsystem.repository.OfferAccessLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditService {

    private final OfferAccessLogRepository repository;

    public AuditService(OfferAccessLogRepository repository) {
        this.repository = repository;
    }

    // bez HTTP (email, scheduler)
    public void log(Offer offer, AuditAction action) {
        OfferAccessLog log = new OfferAccessLog();
        log.setOffer(offer);
        log.setAction(action);
        log.setAccessedAt(LocalDateTime.now());
        repository.save(log);
    }

    // s HTTP kontextem
    public void log(Offer offer, AuditAction action, HttpServletRequest request) {
        OfferAccessLog log = new OfferAccessLog();
        log.setOffer(offer);
        log.setAction(action);
        log.setAccessedAt(LocalDateTime.now());

        if (request != null) {
            log.setIpAddress(request.getRemoteAddr());
            log.setUserAgent(request.getHeader("User-Agent"));
        }

        repository.save(log);
    }
}