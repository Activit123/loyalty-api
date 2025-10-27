package com.barlog.loyaltyapi.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class SelectClassRequestDto {
    @NotNull private Long classId;
}