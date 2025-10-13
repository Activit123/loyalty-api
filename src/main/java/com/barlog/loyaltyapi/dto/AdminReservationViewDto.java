package com.barlog.loyaltyapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminReservationViewDto {
    private Long reservationId;
    private Long userId;
    private String tableName;
    private String userEmail; // Sau nume, în funcție de ce doriți să afișați
    private int numberOfGuests;
    private LocalDateTime reservationTime;
    private String status;
}