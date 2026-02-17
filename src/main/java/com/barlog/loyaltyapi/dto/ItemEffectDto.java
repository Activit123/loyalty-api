package com.barlog.loyaltyapi.dto;

import com.barlog.loyaltyapi.model.ItemEffectType;
import com.barlog.loyaltyapi.model.ProductCategory;
import lombok.Data;

@Data
public class ItemEffectDto {
    private ItemEffectType effectType;
    private Double value;
    private ProductCategory targetCategory; // Poate fi null
}