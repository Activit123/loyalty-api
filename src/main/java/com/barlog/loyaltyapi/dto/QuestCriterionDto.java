package com.barlog.loyaltyapi.dto;

import com.barlog.loyaltyapi.model.ProductCategory;
import com.barlog.loyaltyapi.model.QuestType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestCriterionDto {
    @NotNull private QuestType criterionType;
    private ProductCategory targetCategory; // Opțional
    private Long targetProductId;          // Opțional
    
    @NotNull @Min(0) private Double requiredAmount;
}