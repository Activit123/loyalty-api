package com.barlog.loyaltyapi.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TournamentDto {
    private Long id;
    private String title;
    private String gameName;
    private String description;
    private LocalDateTime startTime;
    private Integer maxPlayers;
    private Integer entryFeeCoins;
    
    // Detalii Premii
    private Integer prizeCoins;
    private String prizeDescription;
    private Double rewardXp;
    private Long rewardItemId;
    private String rewardItemName; // Trimis pentru afișare
    private Long rewardProductId;
    private String rewardProductName; // Trimis pentru afișare
    
    private String status;
    private Integer enrolledCount;
}