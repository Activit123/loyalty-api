package com.barlog.loyaltyapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Am transformat 'record' într-o clasă standard
// Adnotarea @Data de la Lombok va genera gettere, settere, constructor, etc.
@Data
public class ClaimRequestDto {

        @NotNull(message = "Amount cannot be null")
        @Min(1)
        private Integer amount;

        private String description;
}