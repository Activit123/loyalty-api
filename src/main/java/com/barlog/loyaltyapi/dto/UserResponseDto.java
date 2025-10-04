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
    private Integer silverCoins;
    private Boolean hasGoldSubscription;
    private Role role;
    private LocalDateTime createdAt;
}