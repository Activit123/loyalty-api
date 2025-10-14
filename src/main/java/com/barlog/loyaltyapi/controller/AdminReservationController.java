package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.AdminCreateReservationRequestDto;
import com.barlog.loyaltyapi.dto.AdminReservationViewDto;
import com.barlog.loyaltyapi.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<AdminReservationViewDto> createReservationByAdmin(@RequestBody AdminCreateReservationRequestDto request) {
        AdminReservationViewDto createdReservation = reservationService.createReservationByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReservation);
    }

    @GetMapping
    public ResponseEntity<List<AdminReservationViewDto>> getAllReservations() {
        List<AdminReservationViewDto> reservations = reservationService.getAllReservations();
        return ResponseEntity.ok(reservations);
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long reservationId) {
        reservationService.cancelReservationById(reservationId);
        return ResponseEntity.noContent().build();
    }
}