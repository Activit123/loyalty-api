package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.AddCoinsRequestDto;
import com.barlog.loyaltyapi.dto.AdjustCoinsRequestDto;
import com.barlog.loyaltyapi.dto.UserResponseDto;
import com.barlog.loyaltyapi.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/actions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminActionsController {
    private final AdminService adminService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponseDto> getUserDetails(@PathVariable Long userId) {
        UserResponseDto userDto = adminService.getUserById(userId);
        return ResponseEntity.ok(userDto);
    }
    @GetMapping("/users/by-email/{email}")
    public ResponseEntity<UserResponseDto> getUserDetailsByEmail(@PathVariable String email) {
        UserResponseDto userDto = adminService.getUserByEmail(email);
        return ResponseEntity.ok(userDto);
    }
    @PostMapping("/users/by-email/{email}/add-coins")
    public ResponseEntity<UserResponseDto> addCoinsToUserByEmail(
            @PathVariable String email,
            @Valid @RequestBody AddCoinsRequestDto addCoinsRequest) {
        UserResponseDto updatedUser = adminService.addCoinsToUserByEmail(email, addCoinsRequest);
        return ResponseEntity.ok(updatedUser);
    }
    @PostMapping("/users/{userId}/add-coins")
    public ResponseEntity<UserResponseDto> addCoinsToUser(
            @PathVariable Long userId,
            @Valid @RequestBody AddCoinsRequestDto addCoinsRequest) {
        UserResponseDto updatedUser = adminService.addCoinsToUser(userId, addCoinsRequest);
        return ResponseEntity.ok(updatedUser);
    }
    @PatchMapping("/{userId}/coins")
    public ResponseEntity<UserResponseDto> adjustCoinBalance(
            @PathVariable Long userId,
            @Valid @RequestBody AdjustCoinsRequestDto request) {
        UserResponseDto updatedUser = adminService.adjustUserCoins(userId, request);
        return ResponseEntity.ok(updatedUser);
    }
}