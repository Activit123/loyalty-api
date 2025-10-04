package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.AuthResponseDto;
import com.barlog.loyaltyapi.dto.LoginRequestDto;
import com.barlog.loyaltyapi.dto.RegisterUserDto;
import com.barlog.loyaltyapi.dto.UserResponseDto;
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
    public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody RegisterUserDto registerUserDto) {
        // Apelăm serviciul pentru a înregistra utilizatorul
        User newUser = userService.registerUser(registerUserDto);

        // Mapăm entitatea User la DTO-ul de răspuns
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(newUser.getId());
        responseDto.setFirstName(newUser.getFirstName());
        responseDto.setLastName(newUser.getLastName());
        responseDto.setEmail(newUser.getEmail());
        responseDto.setSilverCoins(newUser.getSilverCoins());
        responseDto.setHasGoldSubscription(newUser.getHasGoldSubscription());
        responseDto.setRole(newUser.getRole());
        responseDto.setCreatedAt(newUser.getCreatedAt());

        // Returnăm DTO-ul de răspuns cu statusul HTTP 201 Created
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()
                )
        );
        var user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(); // Utilizatorul ar trebui să existe după autentificare
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