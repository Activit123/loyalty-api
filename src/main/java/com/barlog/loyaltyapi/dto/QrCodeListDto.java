package com.barlog.loyaltyapi.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class QrCodeListDto {
    private UUID id;
    private String itemName;
    private LocalDateTime createdAt;
    private boolean isUsed;
    private String usedByEmail;
    private LocalDateTime usedAt;
}