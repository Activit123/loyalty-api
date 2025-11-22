package com.barlog.loyaltyapi.dto;

import lombok.Data;

@Data
public class UserCriterionProgressDto {
    private Long criterionId;
    private String criterionDescription; // Ex: "Cumpără 2 Pale Ale"
    private Double currentProgress;
    private Double requiredAmount;
    private Integer progressPercentage; // (currentProgress / requiredAmount) * 100
    private Boolean isCompleted;
}