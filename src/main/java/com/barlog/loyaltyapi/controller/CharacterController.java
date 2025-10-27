package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.ClassTypeDto;
import com.barlog.loyaltyapi.dto.NicknameRequestDto;
import com.barlog.loyaltyapi.dto.RaceDto;
import com.barlog.loyaltyapi.dto.SelectClassRequestDto;
import com.barlog.loyaltyapi.dto.SelectRaceRequestDto;
import com.barlog.loyaltyapi.exception.ResourceNotFoundException;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.service.CharacterService;
import com.barlog.loyaltyapi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/character")
@RequiredArgsConstructor
public class CharacterController {

    private final CharacterService characterService;
    private final UserService userService;

    // Endpoint public pentru a vedea informațiile despre rase
    @GetMapping("/races")
    public ResponseEntity<List<RaceDto>> getAllRaces() {
        return ResponseEntity.ok(characterService.getAllRaces());
    }

    // Endpoint public pentru a vedea informațiile despre clase
    @GetMapping("/classes")
    public ResponseEntity<List<ClassTypeDto>> getAllClassTypes() {
        return ResponseEntity.ok(characterService.getAllClassTypes());
    }

    // Endpoint securizat pentru ca un utilizator să-și aleagă rasa
    @PostMapping("/select-race")
    public ResponseEntity<String> selectRace(Authentication authentication, @RequestBody @Valid SelectRaceRequestDto request) {
        User currentUser = (User) authentication.getPrincipal();
        try {
            characterService.selectRace(currentUser, request.getRaceId());
            return ResponseEntity.ok("Rasa a fost aleasă cu succes.");
        } catch (IllegalStateException | ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // Endpoint securizat pentru ca un utilizator să-și aleagă clasa
    @PostMapping("/select-class")
    public ResponseEntity<String> selectClass(Authentication authentication, @RequestBody @Valid SelectClassRequestDto request) {
        User currentUser = (User) authentication.getPrincipal();
        try {
            characterService.selectClass(currentUser, request.getClassId());
            return ResponseEntity.ok("Clasa a fost aleasă cu succes.");
        } catch (IllegalStateException | ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint securizat pentru a seta/schimba nickname-ul
    @PutMapping("/nickname")
    public ResponseEntity<String> updateUserNickname(Authentication authentication, @RequestBody @Valid NicknameRequestDto nicknameDto) {
        User currentUser = (User) authentication.getPrincipal();
        try {
            userService.updateNickname(currentUser, nicknameDto.getNickname());
            return ResponseEntity.ok("Nickname actualizat cu succes.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}