package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.BoardGameDto;
import com.barlog.loyaltyapi.dto.BoardGameRequestDto;
import com.barlog.loyaltyapi.exception.ResourceNotFoundException;
import com.barlog.loyaltyapi.model.BoardGame;
import com.barlog.loyaltyapi.repository.BoardGameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
// Import-ul ServletUriComponentsBuilder nu mai este necesar
// import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardGameService {

    private final BoardGameRepository boardGameRepository;
    private final FileStorageService fileStorageService; // Acum este serviciul Cloudinary

    // --- Metode de mapare ---

    private BoardGameDto convertToDto(BoardGame game) {
        BoardGameDto dto = new BoardGameDto();

        // *** GENERARE DINAMICĂ A URL-ULUI ***
        // Presupunem că game.getImageUrl() stochează acum Public ID-ul Cloudinary
        String imageUrl = null;
        if (game.getImageUrl() != null && !game.getImageUrl().isEmpty()) {
            imageUrl = fileStorageService.getImageUrlFromPublicId(game.getImageUrl());
        }

        dto.setId(game.getId());
        dto.setName(game.getName());
        dto.setDescription(game.getDescription());
        dto.setImageUrl(imageUrl); // Setează URL-ul generat
        dto.setPlayers(game.getPlayers());
        dto.setPlayTime(game.getPlayTime());
        dto.setAgeLimit(game.getAgeLimit());
        dto.setCategory(game.getCategory());
        dto.setComplexityRating(game.getComplexityRating());
        dto.setCreatedAt(game.getCreatedAt());
        return dto;
    }

    private BoardGame convertToEntity(BoardGameRequestDto dto, String publicId) {
        return BoardGame.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .imageUrl(publicId) // *** SALVĂM PUBLIC ID-UL ***
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
        // Obține Public ID-ul Cloudinary
        String publicId = fileStorageService.storeFile(imageFile);
        BoardGame game = convertToEntity(requestDto, publicId);
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

            // --- LOGICĂ OPȚIONALĂ: ȘTERGE IMAGINEA VECHE ---
            if (game.getImageUrl() != null && !game.getImageUrl().isEmpty()) {
                fileStorageService.deleteFile(game.getImageUrl());
            }
            // -------------------------------------------------

            String newPublicId = fileStorageService.storeFile(imageFile);
            game.setImageUrl(newPublicId); // Stochează noul Public ID
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

        // --- LOGICĂ OPȚIONALĂ: ȘTERGE IMAGINEA DE PE CLOUDINARY ---
        if (game.getImageUrl() != null && !game.getImageUrl().isEmpty()) {
            fileStorageService.deleteFile(game.getImageUrl());
        }
        // ----------------------------------------------------------

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