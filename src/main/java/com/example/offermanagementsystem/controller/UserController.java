package com.example.offermanagementsystem.controller;

import com.example.offermanagementsystem.model.User;
import com.example.offermanagementsystem.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =====================================================
    // 👤 PROFIL PŘIHLÁŠENÉHO UŽIVATELE
    // =====================================================
    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        return "users/profile";
    }

    // =====================================================
    // 🔐 ZMĚNA HESLA
    // =====================================================
    @PostMapping("/change-password")
    public String changePassword(
            Authentication authentication,
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            Model model
    ) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            model.addAttribute("error", "Staré heslo není správné");
            model.addAttribute("user", user);
            return "users/profile";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "redirect:/users/profile?passwordChanged";
    }

    // =====================================================
    // 👮 ADMIN – SEZNAM UŽIVATELŮ
    // =====================================================
    @GetMapping("/admin/list")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    // =====================================================
    // ❌ ADMIN – SMAZÁNÍ UŽIVATELE
    // =====================================================
    @PostMapping("/admin/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/users/admin/list";
    }
}