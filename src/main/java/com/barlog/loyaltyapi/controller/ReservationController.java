package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.CreateReservationRequest;
import com.barlog.loyaltyapi.dto.UserReservationDto;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * Endpoint securizat pentru ca un utilizator să își creeze propria rezervare.
     * ID-ul utilizatorului este preluat automat din token-ul de autentificare.
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> createReservation(
            @RequestBody CreateReservationRequest request,
            @AuthenticationPrincipal User authenticatedUser) {

        reservationService.createReservation(authenticatedUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Endpoint securizat pentru ca un utilizator să își vizualizeze rezervarea activă.
     */
    @GetMapping("/my-reservation")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserReservationDto> getMyReservation(@AuthenticationPrincipal User authenticatedUser) {
        // Apelăm metoda corectă din serviciu
        UserReservationDto reservationDto = reservationService.getMyReservation(authenticatedUser);

        // Verificăm dacă utilizatorul are o rezervare
        if (reservationDto == null) {
            // Dacă nu are, returnăm un status 204 No Content, ceea ce indică
            // că cererea a fost procesată cu succes, dar nu există conținut de afișat.
            return ResponseEntity.noContent().build();
        }

        // Dacă are o rezervare, o returnăm cu status 200 OK.
        return ResponseEntity.ok(reservationDto);
    }

    /**
     * Endpoint securizat pentru ca un utilizator să își anuleze rezervarea activă.
     */
    @DeleteMapping("/my-reservation")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> cancelMyReservation(@AuthenticationPrincipal User user) {
        reservationService.cancelMyReservation(user);
        return ResponseEntity.noContent().build();
    }
}