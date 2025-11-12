// src/main/java/com/barlog/loyaltyapi/dto/MenuItemRequestDto.java
package com.barlog.loyaltyapi.dto;

import com.barlog.loyaltyapi.model.MenuItemCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MenuItemRequestDto(
        @NotBlank(message = "Numele este obligatoriu")
        String name,
        
        String description, // Poate fi null
        
        @NotNull(message = "Pretul este obligatoriu")
        @Min(0)
        Integer price,
        
        @NotNull(message = "Categoria este obligatorie")
        MenuItemCategory category,
        
        String volume, // Poate fi null
        
        String icon, // Poate fi null
        
        @NotNull(message = "Ordinea în meniu este obligatorie")
        @Min(0)
        Integer orderInMenu,
        
        boolean isActive // Pentru a activa/dezactiva din admin
) {}