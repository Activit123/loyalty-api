package com.barlog.loyaltyapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BoardGameRequestDto {
    @NotBlank(message = "Numele este obligatoriu")
    private String name;

    @NotBlank(message = "Descrierea este obligatorie")
    private String description;
    
    // imageUrl nu este inclus aici, se gestionează separat ca MultipartFile
    
    @NotBlank(message = "Numărul de jucători este obligatoriu")
    private String players;

    @NotBlank(message = "Timpul de joc este obligatoriu")
    private String playTime;

    @NotBlank(message = "Limita de vârstă este obligatorie")
    private String ageLimit;

    @NotBlank(message = "Categoria este obligatorie")
    private String category; // Initiati, Maestri, Legende
    
    private Double complexityRating; // Poate fi null
}