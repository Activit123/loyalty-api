package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.BoardGameDto;
import com.barlog.loyaltyapi.dto.BoardGameRequestDto;
import com.barlog.loyaltyapi.service.BoardGameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class BoardGameController {

    private final BoardGameService boardGameService;

    // --- Endpoint Public: Toate Jocurile ---
    /**
     * Obține lista completă de jocuri de societate. Vizibil public.
     */
    @GetMapping
    public ResponseEntity<List<BoardGameDto>> getAllGames() {
        List<BoardGameDto> games = boardGameService.getAllGames();
        return ResponseEntity.ok(games);
    }

    // --- Endpoint Public: Joc după ID ---
    @GetMapping("/{id}")
    public ResponseEntity<BoardGameDto> getGameById(@PathVariable Long id) {
        BoardGameDto game = boardGameService.getGameById(id);
        return ResponseEntity.ok(game);
    }

    // --- Endpoint Admin: Creare Joc ---
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BoardGameDto> createGame(
            @RequestPart("gameData") @Valid BoardGameRequestDto requestDto,
            @RequestPart("imageFile") MultipartFile imageFile) throws IOException {
        BoardGameDto createdGame = boardGameService.createGame(requestDto, imageFile);
        return new ResponseEntity<>(createdGame, HttpStatus.CREATED);
    }

    // --- Endpoint Admin: Actualizare Joc ---
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BoardGameDto> updateGame(
            @PathVariable Long id,
            @RequestPart("gameData") @Valid BoardGameRequestDto requestDto,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) throws IOException {
        BoardGameDto updatedGame = boardGameService.updateGame(id, requestDto, imageFile);
        return ResponseEntity.ok(updatedGame);
    }

    // --- Endpoint Admin: Ștergere Joc ---
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGame(@PathVariable Long id) {
        boardGameService.deleteGame(id);
        return ResponseEntity.noContent().build();
    }
}