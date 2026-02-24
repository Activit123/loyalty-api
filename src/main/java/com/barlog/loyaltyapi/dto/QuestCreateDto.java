package com.barlog.loyaltyapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class QuestCreateDto {
    @NotBlank private String title;
    private String description;
    @Min(1) private Integer durationDays;
    @NotNull private String type; // QuestType principal (pentru frontend)

    // Recompense
    @Min(0) private Integer rewardCoins;
    @Min(0) private Double rewardXp;
    private Long rewardProductId;
    private Long rewardItemTemplateId; // ID-ul itemului de dat ca premiu
    private boolean isActive = true;
    
    // Lista de Criterii (Cererile multiple)
    @NotNull private List<QuestCriterionDto> criteria; 
}