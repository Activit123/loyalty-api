package com.barlog.loyaltyapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserLeaderboardDto {
    private String firstName;
    private String lastName;
    private String email;
    private Integer coins; // Am schimbat din loyaltyPoints în coins
    private Double experience;
    private String nickname;
    private String avatarUrl;
    private LevelInfoDto levelInfo;
}