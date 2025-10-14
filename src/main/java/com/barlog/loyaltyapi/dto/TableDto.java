package com.barlog.loyaltyapi.dto;
import com.barlog.loyaltyapi.model.TableStatus;
import lombok.*;
@Data @AllArgsConstructor
public class TableDto {
    private Long id;
    private String name;
    private int capacity;
    private TableStatus status;
}