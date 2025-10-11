package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.UserResponseDto;
import com.barlog.loyaltyapi.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(Authentication authentication) {
        // Spring Security injectează obiectul de 'Authentication' care conține detaliile utilizatorului logat.
        User currentUser = (User) authentication.getPrincipal();

        // Mapăm entitatea la un DTO pentru a nu expune date sensibile
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(currentUser.getId());
        responseDto.setFirstName(currentUser.getFirstName());
        responseDto.setLastName(currentUser.getLastName());
        responseDto.setEmail(currentUser.getEmail());
        responseDto.setCoins(currentUser.getCoins());
        responseDto.setRole(currentUser.getRole());
        responseDto.setCreatedAt(currentUser.getCreatedAt());

        return ResponseEntity.ok(responseDto);
    }
}