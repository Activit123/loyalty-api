package com.barlog.loyaltyapi.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data @AllArgsConstructor
public class LevelInfoDto {
    private int level;
    private long currentXpInLevel;
    private long xpForNextLevel;
    private int progressPercentage;
}