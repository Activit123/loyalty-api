package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.AuthResponseDto;
import com.barlog.loyaltyapi.dto.LoginRequestDto;
import com.barlog.loyaltyapi.dto.RegisterUserDto;
import com.barlog.loyaltyapi.dto.UserResponseDto;
import com.barlog.loyaltyapi.model.AuthProvider;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.repository.UserRepository;
import com.barlog.loyaltyapi.security.JwtService;
import com.barlog.loyaltyapi.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository; // Adaugă și repo-ul
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterUserDto registerUserDto) {
        try {
            User newUser = userService.registerUser(registerUserDto);

            // Mapăm entitatea User la DTO-ul de răspuns (acest cod ar trebui mutat într-un mapper, dar e ok pentru acum)
            UserResponseDto responseDto = new UserResponseDto();
            responseDto.setId(newUser.getId());
            responseDto.setFirstName(newUser.getFirstName());
            responseDto.setLastName(newUser.getLastName());
            responseDto.setEmail(newUser.getEmail());
            responseDto.setCoins(newUser.getCoins());
            responseDto.setRole(newUser.getRole());
            responseDto.setCreatedAt(newUser.getCreatedAt());

            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            // Prindem excepția aruncată de serviciu și returnăm un mesaj clar
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        var userOptional = userRepository.findByEmail(loginRequestDto.getEmail());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credentiale invalide");
        }

        var user = userOptional.get();
        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Acest cont este inregistrat prin Google. Folositi optiunea de login cu Google.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()
                )
        );

        var jwtToken = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponseDto(jwtToken));
    }
    // În AuthController.java
    @GetMapping("/test")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Hello, authenticated user!");
    }

}