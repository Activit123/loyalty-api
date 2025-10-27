package com.barlog.loyaltyapi.dto;

import com.barlog.loyaltyapi.model.ProductCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductRequestDto(
        @NotBlank(message = "Numele este obligatoriu")
        String name,
        
        String description,
        
        @NotNull(message = "Pretul de cumparare este obligatoriu")
        @Min(0)
        Integer buyPrice,
        
        @NotNull(message = "Valoarea de revendicare este obligatorie")
        @Min(0)
        Integer claimValue,
        
        @NotNull(message = "Stocul este obligatoriu (-1 pentru nelimitat)")
        Integer stock,
        @NotNull(message = "Categoria este obligatorie")
        ProductCategory category
) {}