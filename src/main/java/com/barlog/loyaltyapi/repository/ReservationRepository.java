package com.barlog.loyaltyapi.repository;

import com.barlog.loyaltyapi.model.Reservation;
import com.barlog.loyaltyapi.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // Caută rezervarea activă a unui utilizator
    Optional<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status);

    boolean existsByUserIdAndStatus(Long id, ReservationStatus reservationStatus);
}