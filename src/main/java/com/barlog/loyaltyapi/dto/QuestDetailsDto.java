package com.barlog.loyaltyapi.dto;

import com.barlog.loyaltyapi.model.QuestType;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuestDetailsDto {
    private Long id;
    private String title;
    private String description;
    private Integer durationDays;
    private QuestType type;

    // Recompense
    private Integer rewardCoins;
    private Double rewardXp;

    private Long rewardProductId;
    private String rewardProductName;

    // ADĂUGAT: ID-ul template-ului RPG (pentru Admin Edit)
    private Long rewardItemTemplateId;

    private boolean isActive;
    private LocalDateTime createdAt;
    private List<QuestCriterionDto> criteria;
}