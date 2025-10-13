package com.barlog.loyaltyapi.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class CreateReservationRequest {
    private Long tableId;
    private int numberOfGuests;
    private LocalDateTime reservationTime;
}