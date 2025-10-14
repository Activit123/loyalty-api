package com.barlog.loyaltyapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetailsDto {
    private Long transactionId;
    private String userEmail; // Email-ul utilizatorului care a făcut tranzacția
    private Integer amount;
    private LocalDateTime transactionDate;
    private String description;
}