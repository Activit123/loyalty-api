package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.ResetRequestDto;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.repository.UserRepository;
import com.barlog.loyaltyapi.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth/reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetRequestDto request) {
        
        // 1. Caută userul după email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul nu a fost găsit."));

        // 2. Verifică Cheia de Salvare
        if (!user.getRecoveryKey().equals(request.getRecoveryKey())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cheie de salvare invalidă.");
        }
        
        // 3. Verifică Reutilizarea
        if (user.getRecoveryKeyLastUsed() != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cheia de salvare a fost deja utilizată și nu poate fi refolosită.");
        }
        
        // 4. Aplică parola nouă
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setRecoveryKeyLastUsed(LocalDateTime.now()); // Marchează ca folosită
        userRepository.save(user);

        return ResponseEntity.ok("Parola a fost resetată cu succes. Te poți autentifica.");
    }
}