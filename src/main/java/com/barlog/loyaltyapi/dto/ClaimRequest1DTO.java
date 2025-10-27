package com.barlog.loyaltyapi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class ClaimRequest1DTO {
    @NotNull(message = "Claim UID is required")
    private UUID claimUid;
}