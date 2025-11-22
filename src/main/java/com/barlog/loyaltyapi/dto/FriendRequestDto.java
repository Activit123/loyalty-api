package com.barlog.loyaltyapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FriendRequestDto {
    @NotBlank(message = "Identifier cannot be blank")
    private String identifier; // Poate fi email sau nickname (sau valoarea din QR, care e email)
}