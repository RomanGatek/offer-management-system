package com.example.offermanagementsystem.service;

import com.example.offermanagementsystem.model.Offer;
import com.example.offermanagementsystem.model.OfferStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // 🔹 PŮVODNÍ METODA (může zůstat)
    public void sendStatusEmail(Offer offer) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(offer.getCustomerEmail());
        msg.setSubject("Stav nabídky: " + offer.getStatus());
        msg.setText(buildText(offer));

        mailSender.send(msg);
    }

    // ✅ NOVÁ BEZPEČNÁ METODA
    public void sendStatusEmailSafe(Offer offer) {
        try {
            sendStatusEmail(offer);
        } catch (MailException e) {
            // log – NEHODIT aplikaci
            System.err.println(
                    "Nepodařilo se odeslat email k nabídce ID="
                            + offer.getId() + ": " + e.getMessage()
            );
        }
    }

    // 🔹 VYTAŽENÝ TEXT – přehlednější
    private String buildText(Offer offer) {
        return switch (offer.getStatus()) {

            case ODESLANA -> """
                    Dobrý den %s,

                    Vaše nabídka byla ODESLÁNA.
                    Cena: %s Kč

                    S pozdravem
                    """.formatted(
                    offer.getCustomerName(),
                    offer.getTotalPrice()
            );

            case PRIJATA -> """
                    Dobrý den %s,

                    Vaše nabídka byla PŘIJATA 🎉
                    Ozveme se s dalšími kroky.
                    """.formatted(offer.getCustomerName());

            case ZAMITNUTA -> """
                    Dobrý den %s,

                    Vaše nabídka byla ZAMÍTNUTA.
                    """.formatted(offer.getCustomerName());

            default -> "";
        };
    }
}