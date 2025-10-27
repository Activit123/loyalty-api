package com.barlog.loyaltyapi.dto;
import lombok.Data;
@Data
public class ClassTypeDto {
    private Long id;
    private String name;
    private String description;
    private String requiredAttribute;
}