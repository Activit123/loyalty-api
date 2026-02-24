package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.CreateTournamentRequest;
import com.barlog.loyaltyapi.dto.TournamentDto;
import com.barlog.loyaltyapi.dto.TournamentMatchDto;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.service.TournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;

    @GetMapping
    public ResponseEntity<List<TournamentDto>> getAllTournaments() {
        return ResponseEntity.ok(tournamentService.getAllTournaments());
    }

    @GetMapping("/{id}/bracket")
    public ResponseEntity<List<TournamentMatchDto>> getBracket(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.getBracket(id));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<String> joinTournament(Authentication authentication, @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        try {
            tournamentService.joinTournament(user, id);
            return ResponseEntity.ok("Te-ai înscris cu succes!");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- Rute Admin ---
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TournamentDto> createTournament(@RequestBody CreateTournamentRequest request) {
        return ResponseEntity.ok(tournamentService.createTournament(request));
    }

    @PostMapping("/admin/{id}/start")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> startTournament(@PathVariable Long id) {
        try {
            tournamentService.generateBracket(id);
            return ResponseEntity.ok("Turneu început și bracket generat.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/admin/matches/{matchId}/winner/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> setMatchWinner(@PathVariable Long matchId, @PathVariable Long userId) {
        try {
            tournamentService.setMatchWinner(matchId, userId);
            return ResponseEntity.ok("Câștigător setat.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}