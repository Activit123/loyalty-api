package com.barlog.loyaltyapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class UserReservationDto {
    private Long reservationId;
    private String tableName;
    private int numberOfGuests;
    private LocalDateTime reservationTime;
}