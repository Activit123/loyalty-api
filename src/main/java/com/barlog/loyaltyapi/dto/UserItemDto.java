package com.barlog.loyaltyapi.dto;

import com.barlog.loyaltyapi.model.ItemRarity;
import com.barlog.loyaltyapi.model.ItemSlot;
import lombok.Data;
import java.util.List;

@Data
public class UserItemDto {
    private Long id; // ID-ul unic al instanței (UserItem)
    private Long templateId; // ID-ul definiției (ItemTemplate)
    private String name;
    private String description;
    private String imageUrl;
    private ItemSlot slot;
    private ItemRarity rarity;
    private boolean isEquipped;
    private Integer minLevel;
    private Integer reqStr;
    private Integer reqDex;
    private Integer reqInt;
    private Integer reqCha;
    private List<ItemEffectDto> effects;
}