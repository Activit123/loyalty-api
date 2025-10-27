package com.barlog.loyaltyapi.service;
import com.barlog.loyaltyapi.dto.LevelInfoDto;
import org.springframework.stereotype.Service;

@Service
public class LevelService {
    private long xpForLevel(int level) {
        if (level <= 1) return 0;
        return Math.round(100 * Math.pow(level - 1, 1.5));
    }
    public LevelInfoDto calculateLevelInfo(long totalXp) {
        int level = 1;
        while (totalXp >= xpForLevel(level + 1)) {
            level++;
        }
        long xpForCurrentLevel = xpForLevel(level);
        long xpForNextLevel = xpForLevel(level + 1);
        long xpProgress = totalXp - xpForCurrentLevel;
        long xpToNext = xpForNextLevel - xpForCurrentLevel;
        int progressPercentage = (xpToNext > 0) ? (int) Math.round(((double) xpProgress / xpToNext) * 100) : 100;
        return new LevelInfoDto(level, xpProgress, xpToNext, progressPercentage);
    }
}