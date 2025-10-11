package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.RegisterUserDto;
import com.barlog.loyaltyapi.model.AuthProvider;
import com.barlog.loyaltyapi.model.Role;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin-setup")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Injectăm cheia secretă din application.properties
    @Value("${admin.creation.secret-key}")
    private String adminCreationKey;

    @PostMapping("/create-admin")
    public ResponseEntity<String> createAdminAccount(
            @Valid @RequestBody RegisterUserDto registerUserDto,
            @RequestHeader("X-Admin-Secret-Key") String secretKey) {

        // 1. Verificăm cheia secretă din header
        if (secretKey == null || !secretKey.equals(adminCreationKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cheie secreta invalida.");
        }

        // 2. Verificăm dacă există deja un admin
        if (userRepository.existsByRole(Role.ROLE_ADMIN)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Un cont de administrator exista deja.");
        }

        // 3. Verificăm dacă email-ul este deja folosit
        if (userRepository.findByEmail(registerUserDto.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Emailul este deja folosit.");
        }

        // 4. Creăm contul de admin
        User adminUser = User.builder()
                .firstName(registerUserDto.getFirstName())
                .lastName(registerUserDto.getLastName())
                .email(registerUserDto.getEmail())
                .password(passwordEncoder.encode(registerUserDto.getPassword()))
                .role(Role.ROLE_ADMIN) // Setăm rolul ca ADMIN
                .authProvider(AuthProvider.LOCAL)
                .coins(0)
                .build();

        userRepository.save(adminUser);

        return ResponseEntity.status(HttpStatus.CREATED).body("Cont de administrator creat cu succes.");
    }
}