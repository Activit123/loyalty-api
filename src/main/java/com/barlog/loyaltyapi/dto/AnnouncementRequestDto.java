package com.barlog.loyaltyapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnnouncementRequestDto {
    @NotBlank(message = "Titlul nu poate fi gol.")
    private String title;
    @NotBlank(message = "Descrierea nu poate fi goală.")
    private String description;
}