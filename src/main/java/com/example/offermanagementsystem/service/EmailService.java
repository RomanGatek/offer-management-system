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

    // 🔹 VEŘEJNÁ BASE URL (zatím natvrdo – později do configu)
    private static final String BASE_URL = "http://localhost:8080";

    // ===============================
    // BEZPEČNÉ ODESLÁNÍ
    // ===============================
    public void sendStatusEmailSafe(Offer offer) {
        try {
            sendStatusEmail(offer);
        } catch (MailException e) {
            System.err.println(
                    "Nepodařilo se odeslat email k nabídce ID="
                            + offer.getId() + ": " + e.getMessage()
            );
        }
    }

    // ===============================
    // HLAVNÍ EMAIL
    // ===============================
    private void sendStatusEmail(Offer offer) {

        String publicUrl =
                BASE_URL + "/public/offers/" + offer.getCustomerToken();

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(offer.getCustomerEmail());
        msg.setSubject("Stav nabídky: " + offer.getStatus());
        msg.setText(buildText(offer, publicUrl));

        mailSender.send(msg);
    }

    // ===============================
    // TEXT EMAILU
    // ===============================
    private String buildText(Offer offer, String publicUrl) {

        return switch (offer.getStatus()) {

            case ODESLANA -> """
                    Dobrý den %s,

                    Vaše nabídka byla ODESLÁNA.
                    Cena: %s Kč

                    Nabídku si můžete zobrazit zde:
                    %s

                    S pozdravem
                    """.formatted(
                    offer.getCustomerName(),
                    offer.getTotalPrice(),
                    publicUrl
            );

            case PRIJATA -> """
                    Dobrý den %s,

                    Vaše nabídka byla PŘIJATA 🎉

                    Detail nabídky:
                    %s
                    """.formatted(
                    offer.getCustomerName(),
                    publicUrl
            );

            case ZAMITNUTA -> """
                    Dobrý den %s,

                    Vaše nabídka byla ZAMÍTNUTA.

                    Detail nabídky:
                    %s
                    """.formatted(
                    offer.getCustomerName(),
                    publicUrl
            );

            default -> """
                    Dobrý den %s,

                    Došlo ke změně stavu Vaší nabídky.

                    Detail:
                    %s
                    """.formatted(
                    offer.getCustomerName(),
                    publicUrl
            );
        };
    }
}
