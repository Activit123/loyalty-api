package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.*;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.service.AdminService;
import com.barlog.loyaltyapi.service.BonusService;
import com.barlog.loyaltyapi.service.UserService; // Importăm interfața, nu implementarea
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor; // Importăm adnotarea
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor // 1. Adăugăm adnotarea Lombok
public class UserController {

    // 2. Injectăm interfața, nu implementarea specifică
    private final UserService userService;
    private final AdminService adminService;
    private final BonusService bonusService;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);


    @GetMapping("/getAllUsers")
    public ResponseEntity<List<AllUsersDTO>> getAllUsers(){
        List<AllUsersDTO> allUsers = userService.getAllUsers();
        return ResponseEntity.ok(allUsers);
    }
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        // Folosim metoda de mapare din AdminService, care este deja injectat
        UserResponseDto responseDto = adminService.mapUserToDto(currentUser);

        return ResponseEntity.ok(responseDto);
    }
    // ADĂUGAT: Endpoint pentru upload avatar
    @PatchMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponseDto> updateAvatar(
            Authentication authentication,
            @RequestPart("image") MultipartFile imageFile) {

        User currentUser = (User) authentication.getPrincipal();
        User updatedUser = userService.updateAvatar(currentUser, imageFile);

        // Mapăm utilizatorul la DTO pentru a trimite noul URL către client
        return ResponseEntity.ok(adminService.mapUserToDto(updatedUser));
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
    @GetMapping("/me/bonuses")
    public ResponseEntity<ActiveBonusDto> getMyBonuses(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(bonusService.getUserActiveBonuses(user));
    }

    @PatchMapping("/me/generate-recovery-key")
    public ResponseEntity<UserResponseDto> generateNewRecoveryKey(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        // Logica de serviciu pentru a genera o cheie nouă
        User updatedUser = userService.generateNewRecoveryKey(currentUser);

        // DTO-ul va include noul recoveryKey în câmpul recoveryKey
        return ResponseEntity.ok(adminService.mapUserToDto(updatedUser));
    }
    // --- RPG: DISTRIBUIRE PUNCTE (STR, DEX, etc.) ---
    @PostMapping("/distribute-points")
    public ResponseEntity<UserResponseDto> distributePoints(
            Authentication authentication,
            @RequestBody PointDistributionDto request) {

        User user = (User) authentication.getPrincipal();
        userService.distributePoints(user, request);

        // Returnăm userul actualizat pentru a se reflecta în UI instant
        return ResponseEntity.ok(adminService.mapUserToDto(user));
    }
    @PutMapping("/me/nickname")
    public ResponseEntity<?> updateUserNickname(Authentication authentication, @RequestBody @Valid NicknameRequestDto nicknameDto) {
        User currentUser = (User) authentication.getPrincipal();
        try {
            userService.updateNickname(currentUser, nicknameDto.getNickname());
            return ResponseEntity.ok("Nickname actualizat cu succes.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}