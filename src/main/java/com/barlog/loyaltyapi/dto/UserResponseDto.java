package com.barlog.loyaltyapi.dto;

import com.barlog.loyaltyapi.model.Role;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Integer coins;
    // AM ȘTERS: private Boolean hasGoldSubscription;
    // AM ADĂUGAT:
    private Role role;
    private boolean hasPrestige;
    private LocalDateTime createdAt;
    private String nickname;
    private Integer consecutiveActivityDays;
    private Double experience;
    private Double xpRate;
    private RaceDto race; // Vom trimite obiectul complet
    private ClassTypeDto classType;
    private LevelInfoDto levelInfo;
    private String avatarUrl;
    private String recoveryKey;
    private Integer strength;
    private Integer dexterity;
    private Integer intelligence;
    private Integer charisma;
    private Integer unallocatedPoints;

}