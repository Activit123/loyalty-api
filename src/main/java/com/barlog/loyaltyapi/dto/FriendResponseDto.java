package com.barlog.loyaltyapi.dto;

import lombok.Data;

// Reutilizăm DTO-urile existente LevelInfoDto și ClassTypeDto/RaceDto
// Nota: Rasele/Clasele nu sunt neapărat necesare pentru lista de prieteni,
// dar LevelInfo-ul este esențial pentru aspectul RPG.

@Data
public class FriendResponseDto {
    private Long friendshipId;   // ID-ul relației (pentru accept/reject/delete)
    private Long userId;         // ID-ul prietenului/cererii
    private String nickname;
    private String email;
    private Double experience;
    private String status;       // PENDING, ACCEPTED, BLOCKED
    private LevelInfoDto levelInfo;
    private String avatarUrl;
    private Integer coins;
}