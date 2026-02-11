package com.example.offermanagementsystem.service;

import com.example.offermanagementsystem.model.User;
import com.example.offermanagementsystem.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ActorResolverService {

    private final UserRepository userRepository;

    public ActorResolverService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User resolveActor() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null; // zákazník přes token
        }

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElse(null);
    }
}