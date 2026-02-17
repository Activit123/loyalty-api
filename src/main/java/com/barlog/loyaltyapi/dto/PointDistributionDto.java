package com.barlog.loyaltyapi.dto;

import lombok.Data;

@Data
public class PointDistributionDto {
    private int str; // Câte puncte vrea să adauge la Strength
    private int dex;
    private int intel; // "int" e cuvânt rezervat, folosim "intel"
    private int cha;
}