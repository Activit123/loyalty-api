package com.barlog.loyaltyapi.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class SelectRaceRequestDto {
    @NotNull private Long raceId;
}