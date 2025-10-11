package com.barlog.loyaltyapi.dto;

import jakarta.validation.constraints.NotNull;

public record AddCoinsRequestDto(
        @NotNull(message = "amount cannot be null")
        Integer amount,

        String description
) {}