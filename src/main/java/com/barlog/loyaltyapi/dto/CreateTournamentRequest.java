package com.barlog.loyaltyapi.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateTournamentRequest {
    private String title;
    private String gameName;
    private String description;
    private LocalDateTime startTime;
    private Integer maxPlayers;
    private Integer entryFeeCoins;
    
    // Premiile noi
    private String prizeDescription;
    private Integer prizeCoins;
    private Double rewardXp;
    private Long rewardItemId;
    private Long rewardProductId;
}