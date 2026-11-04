package com.davidandriychuk.url_shortener.controller;

import com.davidandriychuk.url_shortener.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.davidandriychuk.url_shortener.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/user")
class UserController{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    UserController(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.userRepository = repository;
        this.passwordEncoder = passwordEncoder;
    }


    @PostMapping("/register")
    public String registerUser(@RequestParam String email, @RequestParam String password) {

        if (userRepository.findByEmail(email).isPresent()) {
            return "Error: Email is already in use!";
        }
        String hashedPassword = passwordEncoder.encode(password);

        User newUser = new User(email, hashedPassword, 1L, LocalDateTime.now());
        userRepository.save(newUser);

        return "User registered successfully!";
    }
}