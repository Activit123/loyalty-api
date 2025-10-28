package com.barlog.loyaltyapi.repository;

import com.barlog.loyaltyapi.model.Reservation;
import com.barlog.loyaltyapi.model.ReservationStatus;
import com.barlog.loyaltyapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByUserAndStatus(User user, ReservationStatus status);
    boolean existsByUserAndStatus(User user, ReservationStatus status);

    List<Reservation> findAllByStatusAndReservationTimeBefore(ReservationStatus status, LocalDateTime time);
    List<Reservation> findByStatusAndReservationTimeBetween(ReservationStatus status, LocalDateTime start, LocalDateTime end);

    // --- AICI ESTE CORECȚIA ---
    // Adăugăm 'nativeQuery = true' și folosim nume de coloane SQL (ex: r.table_id)
    @Query(
            value = "SELECT count(r.*) > 0 FROM reservations r WHERE r.table_id = :tableId AND r.status = 'CONFIRMED' AND r.reservation_time < :endTime AND :startTime < (r.reservation_time + interval '2 hour')",
            nativeQuery = true
    )
    boolean existsOverlappingReservation(Long tableId, LocalDateTime startTime, LocalDateTime endTime);
}