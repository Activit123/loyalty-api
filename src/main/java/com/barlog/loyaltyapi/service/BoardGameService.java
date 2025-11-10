package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.BoardGameDto;
import com.barlog.loyaltyapi.dto.BoardGameRequestDto;
import com.barlog.loyaltyapi.exception.ResourceNotFoundException;
import com.barlog.loyaltyapi.model.BoardGame;
import com.barlog.loyaltyapi.repository.BoardGameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardGameService {

    private final BoardGameRepository boardGameRepository;
    private final FileStorageService fileStorageService; // Reutilizăm pentru imagine

    // --- Metode de mapare ---

    private BoardGameDto convertToDto(BoardGame game) {
        BoardGameDto dto = new BoardGameDto();
        dto.setId(game.getId());
        dto.setName(game.getName());
        dto.setDescription(game.getDescription());
        dto.setImageUrl("http://localhost:8090/uploads/images/"+ game.getImageUrl());
        dto.setPlayers(game.getPlayers());
        dto.setPlayTime(game.getPlayTime());
        dto.setAgeLimit(game.getAgeLimit());
        dto.setCategory(game.getCategory());
        dto.setComplexityRating(game.getComplexityRating());
        dto.setCreatedAt(game.getCreatedAt());
        return dto;
    }

    private BoardGame convertToEntity(BoardGameRequestDto dto, String imageUrl) {
        return BoardGame.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .imageUrl(imageUrl)
                .players(dto.getPlayers())
                .playTime(dto.getPlayTime())
                .ageLimit(dto.getAgeLimit())
                .category(dto.getCategory())
                .complexityRating(dto.getComplexityRating())
                .build();
    }

    // --- Metode CRUD ---

    /**
     * Creează un nou joc de societate.
     */
    public BoardGameDto createGame(BoardGameRequestDto requestDto, MultipartFile imageFile) throws IOException {
        String imageUrl = fileStorageService.storeFile(imageFile);
        BoardGame game = convertToEntity(requestDto, imageUrl);
        BoardGame savedGame = boardGameRepository.save(game);
        return convertToDto(savedGame);
    }

    /**
     * Actualizează un joc existent.
     */
    public BoardGameDto updateGame(Long gameId, BoardGameRequestDto requestDto, MultipartFile imageFile) throws IOException {
        BoardGame game = boardGameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Jocul cu ID-ul " + gameId + " nu a fost găsit."));

        // Actualizare atribute
        game.setName(requestDto.getName());
        game.setDescription(requestDto.getDescription());
        game.setPlayers(requestDto.getPlayers());
        game.setPlayTime(requestDto.getPlayTime());
        game.setAgeLimit(requestDto.getAgeLimit());
        game.setCategory(requestDto.getCategory());
        game.setComplexityRating(requestDto.getComplexityRating());

        // Actualizare imagine, dacă a fost furnizată
        if (imageFile != null && !imageFile.isEmpty()) {
            // Șterge imaginea veche (opțional, dar recomandat)

            String newImageUrl = fileStorageService.storeFile(imageFile);
            game.setImageUrl(newImageUrl);
        }

        BoardGame updatedGame = boardGameRepository.save(game);
        return convertToDto(updatedGame);
    }

    /**
     * Șterge un joc după ID, inclusiv imaginea asociată.
     */
    public void deleteGame(Long gameId) {
        BoardGame game = boardGameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Jocul cu ID-ul " + gameId + " nu a fost găsit."));

        // Șterge imaginea din sistemul de fișiere
        if (game.getImageUrl() != null) {

        }

        boardGameRepository.delete(game);
    }

    /**
     * Returnează toate jocurile sortate după categorie și nume. (Public/Admin)
     */
    public List<BoardGameDto> getAllGames() {
        return boardGameRepository.findAllByOrderByCategoryAscNameAsc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Returnează un singur joc după ID. (Util pentru detalii/editare)
     */
    public BoardGameDto getGameById(Long gameId) {
        BoardGame game = boardGameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Jocul cu ID-ul " + gameId + " nu a fost găsit."));
        return convertToDto(game);
    }
}