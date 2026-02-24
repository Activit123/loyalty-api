package com.barlog.loyaltyapi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAppVersionRequest {
    @NotNull private Integer versionCode;
    @NotNull private String versionName;
    private boolean isCritical;
    private String downloadUrl; // Optional, dacă nu e setat punem default
}