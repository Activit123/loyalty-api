package com.barlog.loyaltyapi.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class AdjustCoinsRequestDto {

    @Min(value = 0, message = "Balanța de monede nu poate fi negativă.")
    private int newCoinBalance;
}