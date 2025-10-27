package com.barlog.loyaltyapi.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class InventoryItemDto {
    private Long id;
    private ProductResponseDto product; // Reutilizăm DTO-ul de produs
    private UUID claimUid;
    private String status;
}