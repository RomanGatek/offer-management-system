package com.example.offermanagementsystem.controller;

import com.example.offermanagementsystem.model.User;
import com.example.offermanagementsystem.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/register")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ================= FORM =================
    @GetMapping
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    // ================= SUBMIT =================
    @PostMapping
    public String register(
            @Valid @ModelAttribute("user") User user,
            BindingResult result
    ) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            result.rejectValue(
                    "username",
                    "error.user",
                    "Uživatel již existuje"
            );
        }

        if (result.hasErrors()) {
            return "auth/register";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");

        userRepository.save(user);
        return "redirect:/login?registered";
    }
}