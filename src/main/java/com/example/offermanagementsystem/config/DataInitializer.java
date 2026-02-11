package com.example.offermanagementsystem.config;

import com.example.offermanagementsystem.model.*;
import com.example.offermanagementsystem.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            UserRepository userRepository,
            OfferRepository offerRepository,
            PasswordEncoder passwordEncoder
    ) {

        return args -> {

            // ===============================
            // USERS
            // ===============================

            User admin = userRepository.findByUsername("admin")
                    .orElseGet(() -> {
                        User u = new User();
                        u.setUsername("admin");
                        u.setPassword(passwordEncoder.encode("admin"));
                        u.setRole("ADMIN"); // ✅ OPRAVENO
                        System.out.println(">>> Admin created");
                        return userRepository.save(u);
                    });

            User user1 = userRepository.findByUsername("user1")
                    .orElseGet(() -> {
                        User u = new User();
                        u.setUsername("user1");
                        u.setPassword(passwordEncoder.encode("user123"));
                        u.setRole("USER"); // ✅ OPRAVENO
                        System.out.println(">>> user1 created");
                        return userRepository.save(u);
                    });

            User user2 = userRepository.findByUsername("user2")
                    .orElseGet(() -> {
                        User u = new User();
                        u.setUsername("user2");
                        u.setPassword(passwordEncoder.encode("user123"));
                        u.setRole("USER"); // ✅ OPRAVENO
                        System.out.println(">>> user2 created");
                        return userRepository.save(u);
                    });

            // ===============================
            // OFFERS
            // ===============================

            if (offerRepository.count() > 0) {
                System.out.println(">>> Offers already exist – skipping");
                return;
            }

            Random random = new Random();
            List<User> users = List.of(admin, user1, user2);

            for (int i = 1; i <= 20; i++) {

                Offer offer = new Offer();

                offer.setCustomerName("Zákazník " + i);
                offer.setCustomerEmail("zakaznik" + i + "@test.cz");
                offer.setDescription("Testovací nabídka číslo " + i);

                offer.setTotalPrice(
                        BigDecimal.valueOf(1000 + random.nextInt(9000))
                );

                offer.setStatus(OfferStatus.NOVA); // 🔥 vždy NOVA
                offer.setRevision(1);
                offer.setInEdit(false);
                offer.setArchived(false);
                offer.setExpired(false);

                // Token validní 30 dní
                offer.setTokenExpiresAt(LocalDateTime.now().plusDays(30));

                // Vlastník
                offer.setUser(users.get(i % users.size()));

                offerRepository.save(offer);
            }

            System.out.println(">>> Test offers created");
        };
    }
}