package com.barlog.loyaltyapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // 409 Conflict este un status bun pentru o regulă de business încălcată
public class ReservationException extends RuntimeException {
    public ReservationException(String message) {
        super(message);
    }
}