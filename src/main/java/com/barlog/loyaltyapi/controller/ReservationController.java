package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.CreateReservationRequest;
import com.barlog.loyaltyapi.dto.UserReservationDto;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<Void> createReservation(
            @RequestBody @Valid CreateReservationRequest request,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        reservationService.createReservation(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/my-reservation")
    public ResponseEntity<UserReservationDto> getMyReservation(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        UserReservationDto reservationDto = reservationService.getMyReservation(currentUser);
        if (reservationDto == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(reservationDto);
    }

    @DeleteMapping("/my-reservation")
    public ResponseEntity<Void> cancelMyReservation(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        reservationService.cancelMyReservation(currentUser);
        return ResponseEntity.noContent().build();
    }
}