package com.example.offermanagementsystem.controller;

import com.example.offermanagementsystem.model.*;
import com.example.offermanagementsystem.repository.OfferAccessLogRepository;
import com.example.offermanagementsystem.repository.OfferRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/track")
public class TrackingPixelController {

    private final OfferRepository offerRepository;
    private final OfferAccessLogRepository accessLogRepository;

    public TrackingPixelController(
            OfferRepository offerRepository,
            OfferAccessLogRepository accessLogRepository
    ) {
        this.offerRepository = offerRepository;
        this.accessLogRepository = accessLogRepository;
    }

    @GetMapping(value = "/open/{token}", produces = MediaType.IMAGE_GIF_VALUE)
    public @ResponseBody byte[] trackOpen(
            @PathVariable String token,
            HttpServletRequest request
    ) {
        offerRepository.findByCustomerToken(token).ifPresent(offer -> {

            if (!accessLogRepository.existsByOfferAndAction(
                    offer, AuditAction.EMAIL_OPENED
            )) {
                OfferAccessLog log = new OfferAccessLog(
                        offer,
                        AuditAction.EMAIL_OPENED,
                        request.getRemoteAddr(),
                        request.getHeader("User-Agent")
                );
                accessLogRepository.save(log);
            }
        });

        return new byte[]{
                71,73,70,56,57,97,1,0,1,0,-128,0,0,-1,-1,-1,0,0,0,
                33,-7,4,1,0,0,0,0,44,0,0,0,0,1,0,1,0,0,2,2,
                68,1,0,59
        };
    }
}