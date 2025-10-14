package com.barlog.loyaltyapi.dto;
import lombok.Data;
@Data
public class CreateTableRequestDto {
    private String name;
    private int capacity;
}