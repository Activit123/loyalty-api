package com.barlog.loyaltyapi.dto;

import com.barlog.loyaltyapi.model.ProductCategory;
import lombok.Data;

@Data
public class ProductResponseDto {
    private Long id;
    private String name;
    private String description;
    private Integer buyPrice;
    private Integer claimValue;
    private Integer stock;
    private ProductCategory category;
    private String imageUrl;
    private boolean isActive;
}