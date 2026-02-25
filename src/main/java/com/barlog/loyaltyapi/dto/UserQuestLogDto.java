package com.barlog.loyaltyapi.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserQuestLogDto {
    private Long id;
    private Long questId;
    private String title;
    private String description;
    private String status; // ACTIVE, COMPLETED, REWARDED
    private LocalDate startDate;
    private LocalDateTime completionDate;
    private Integer durationDays;
    // --- Recompense ---
    private Integer rewardCoins;
    private Double rewardXp;
    private String rewardProductName; // Produs Fizic

    // ADĂUGAT: Numele item-ului RPG pentru afișare
    private String rewardItemName;

    private List<UserCriterionProgressDto> criterionProgress;
}