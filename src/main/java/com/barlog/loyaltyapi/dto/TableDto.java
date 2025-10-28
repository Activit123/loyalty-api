package com.barlog.loyaltyapi.dto;

import lombok.*;

@Data @AllArgsConstructor
public class TableDto {
    private Long id;
    private String name;
    private int capacity;
    private String status; // 'AVAILABLE' sau 'RESERVED'
}