package com.barlog.loyaltyapi.dto;
import lombok.Data;
@Data
public class RaceDto {
    private Long id;
    private String name;
    private String description;
    private String primaryAttribute;
    private String racialBenefit;
}