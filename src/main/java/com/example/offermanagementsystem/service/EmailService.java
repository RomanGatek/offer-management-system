package com.example.offermanagementsystem.service;

import com.example.offermanagementsystem.model.Offer;
import com.example.offermanagementsystem.model.OfferStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendStatusEmailSafe(Offer offer) {
        try {
            sendStatusEmail(offer);
        } catch (Exception e) {
            // LOG ONLY – nikdy nespadne transakce
            System.out.println("MAIL FAILED: " + e.getMessage());
        }
    }

    private void sendStatusEmail(Offer offer) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(offer.getCustomerEmail());
        msg.setSubject("Stav nabídky: " + offer.getStatus());

        msg.setText(switch (offer.getStatus()) {
            case ODESLANA -> "Vaše nabídka byla odeslána.";
            case PRIJATA -> "Vaše nabídka byla přijata.";
            case ZAMITNUTA -> "Vaše nabídka byla zamítnuta.";
            default -> "";
        });

        mailSender.send(msg);
    }
}
