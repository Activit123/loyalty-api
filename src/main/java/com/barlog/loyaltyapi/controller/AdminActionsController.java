package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.*;
import com.barlog.loyaltyapi.service.AdminService;
import com.barlog.loyaltyapi.service.QuestService;
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
    private final QuestService questService;
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponseDto> getUserDetails(@PathVariable Long userId) {
        UserResponseDto userDto = adminService.getUserById(userId);
        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/recalculate-all-stats")
    public ResponseEntity<String> recalculateAllStats() {
        adminService.recalculateStatsForEveryone();
        return ResponseEntity.ok("Statistici și puncte recalculate pentru toți utilizatorii!");
    }

    // 1. Oferă Cadou (Item sau Produs)
    @PostMapping("/gift")
    public ResponseEntity<String> giftUser(@RequestBody AdminGiftRequest request) {
        adminService.giftItemOrProduct(request);
        return ResponseEntity.ok("Cadou trimis cu succes către " + request.getUserEmail());
    }

    // 2. Forțează Completare Quest
    @PostMapping("/quest/force-complete")
    public ResponseEntity<String> forceCompleteQuest(@RequestBody AdminQuestRequest request) {
        questService.adminForceCompleteQuest(request.getUserEmail(), request.getQuestId());
        return ResponseEntity.ok("Quest marcat ca fiind completat pentru " + request.getUserEmail());
    }

    @PostMapping("/sync-stats-retroactive")
    public ResponseEntity<String> syncStatsRetroactive() {
        adminService.recalculateStatsForEveryone();
        return ResponseEntity.ok("Successfully synchronized stats and awarded points for all users based on their levels and races!");
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
    @PostMapping("/users/by-email/add-experience")
    public ResponseEntity<?> addManualExperienceToUserByEmail(
            @Valid @RequestBody ManualXpRequestDto requestDto) {

        adminService.addManualExperienceToUserByEmail(requestDto.getEmail(), requestDto.getAmount());
        return ResponseEntity.ok("XP adăugat cu succes utilizatorului " + requestDto.getEmail());
    }
}