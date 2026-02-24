package com.barlog.loyaltyapi.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppVersionResponseDto {
    private Integer versionCode;
    private String versionName;
    private boolean isCritical; // "Urgent"
    private String downloadUrl;
}
