package com.barlog.loyaltyapi.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminCreateReservationRequestDto {

    private String userEmail;
    private Long tableId;
    private int numberOfGuests;
    private LocalDateTime reservationTime;
}