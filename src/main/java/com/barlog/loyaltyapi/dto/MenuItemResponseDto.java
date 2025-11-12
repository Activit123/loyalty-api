// src/main/java/com/barlog/loyaltyapi/dto/MenuItemResponseDto.java
package com.barlog.loyaltyapi.dto;

import com.barlog.loyaltyapi.model.MenuItemCategory;
import lombok.Data;

@Data
public class MenuItemResponseDto {
    private Long id;
    private String name;
    private String description;
    private Integer price;
    private MenuItemCategory category;
    private String volume;
    private String icon;
    private Integer orderInMenu;
    private boolean isActive;
    
    // Ajutor pentru frontend: afișează display name pentru category
    public String getCategoryDisplayName() {
        return this.category != null ? this.category.getDisplayName() : null;
    }
}