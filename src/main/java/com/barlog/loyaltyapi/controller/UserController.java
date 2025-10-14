package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.ClaimRequestDto;
import com.barlog.loyaltyapi.dto.UserResponseDto;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.service.AdminService;
import com.barlog.loyaltyapi.service.UserService; // Importăm interfața, nu implementarea
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor; // Importăm adnotarea
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody; // Importăm @RequestBody corect
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor // 1. Adăugăm adnotarea Lombok
public class UserController {

    // 2. Injectăm interfața, nu implementarea specifică
    private final UserService userService;
    private final AdminService adminService;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        // Folosim metoda de mapare din AdminService, care este deja injectat
        UserResponseDto responseDto = adminService.mapUserToDto(currentUser);

        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/me/claim-receipt")
    public ResponseEntity<UserResponseDto> claimReceipt(
            Authentication authentication,
            @RequestBody @Valid ClaimRequestDto claimRequest) {

        log.info("Received claim request with amount: {}", claimRequest.getAmount());

        User currentUser = (User) authentication.getPrincipal();
        User updatedUser = userService.claimReceiptCoins(currentUser, claimRequest);

        return ResponseEntity.ok(adminService.mapUserToDto(updatedUser));
    }
}