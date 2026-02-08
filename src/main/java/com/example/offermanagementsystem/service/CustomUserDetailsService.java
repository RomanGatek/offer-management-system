package com.example.offermanagementsystem.service;

import com.example.offermanagementsystem.model.User;
import com.example.offermanagementsystem.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        String role = u.getRole(); // může být "ADMIN", "USER" nebo "ROLE_ADMIN"
        if (role == null || role.isBlank()) {
            role = "USER";
        }

        // ✅ normalizace: přesně 1x ROLE_
        String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;

        return new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getPassword(),
                List.of(new SimpleGrantedAuthority(authority))
        );
    }
}