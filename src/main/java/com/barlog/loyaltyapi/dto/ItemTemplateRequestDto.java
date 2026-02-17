package com.barlog.loyaltyapi.dto;

import com.barlog.loyaltyapi.model.ItemRarity;
import com.barlog.loyaltyapi.model.ItemSlot;
import lombok.Data;
import java.util.List;

@Data
public class ItemTemplateRequestDto {
    private String name;
    private String description;
    private ItemSlot slot;
    private ItemRarity rarity;
    private Integer minLevel;
    private Integer buyPrice;
    private Integer sellPrice;
    private boolean isActive;
    private Integer reqStr;
    private Integer reqDex;
    private Integer reqInt;
    private Integer reqCha;
    
    // Lista de efecte
    private List<ItemEffectDto> effects;
}