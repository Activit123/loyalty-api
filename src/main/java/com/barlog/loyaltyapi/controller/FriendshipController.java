package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.FriendRequestDto;
import com.barlog.loyaltyapi.dto.FriendResponseDto;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.service.FriendshipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FriendshipController {
    
    private final FriendshipService friendshipService;

    // --- 1. Trimitere Cerere Prietenie ---
    @PostMapping("/add")
    public ResponseEntity<FriendResponseDto> sendFriendRequest(
            Authentication authentication,
            @Valid @RequestBody FriendRequestDto requestDto) {
        
        User sender = (User) authentication.getPrincipal();
        FriendResponseDto response = friendshipService.sendFriendRequest(sender, requestDto.getIdentifier());
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    // --- 2. Acceptare Cerere Prietenie ---
    @PostMapping("/accept/{requestId}")
    public ResponseEntity<FriendResponseDto> acceptFriendRequest(
            Authentication authentication,
            @PathVariable Long requestId) {
        
        User currentUser = (User) authentication.getPrincipal();
        FriendResponseDto response = friendshipService.acceptFriendRequest(currentUser, requestId);
        
        return ResponseEntity.ok(response);
    }

    // --- 3. Ștergere/Anulare/Respingere Relație ---
    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> deleteFriendship(
            Authentication authentication,
            @PathVariable Long requestId) {
        
        User currentUser = (User) authentication.getPrincipal();
        friendshipService.deleteFriendship(currentUser, requestId);
        
        return ResponseEntity.noContent().build();
    }

    // --- 4. Vizualizare Listă Prieteni și Cereri ---
    @GetMapping
    public ResponseEntity<List<FriendResponseDto>> getFriendshipList(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<FriendResponseDto> list = friendshipService.getFriendshipList(currentUser);
        
        return ResponseEntity.ok(list);
    }
}