package com.barlog.loyaltyapi.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data @AllArgsConstructor
public class LevelInfoDto {
    private int level;
    private double currentXpInLevel;
    private double xpForNextLevel;
    private int progressPercentage;
}