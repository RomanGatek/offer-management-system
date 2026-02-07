package com.example.offermanagementsystem.config;

import com.example.offermanagementsystem.model.User;
import com.example.offermanagementsystem.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository,
                                PasswordEncoder passwordEncoder) {

        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {

                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin")); // 🔐 DŮLEŽITÉ
                admin.setRole("ADMIN");

                userRepository.save(admin);
                System.out.println(">>> Admin created");
            } else {
                System.out.println(">>> Admin already exists");
            }
        };
    }
}