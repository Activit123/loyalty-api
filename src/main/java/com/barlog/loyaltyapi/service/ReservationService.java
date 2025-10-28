package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.*;
import com.barlog.loyaltyapi.exception.ReservationException;
import com.barlog.loyaltyapi.exception.ResourceNotFoundException;
import com.barlog.loyaltyapi.model.*;
import com.barlog.loyaltyapi.repository.ReservationRepository;
import com.barlog.loyaltyapi.repository.TableRepository;
import com.barlog.loyaltyapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final TableRepository tableRepository;
    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private static final int RESERVATION_DURATION_HOURS = 2;

    @Transactional
    public void createReservation(User user, CreateReservationRequest request) {
        if (reservationRepository.existsByUserAndStatus(user, ReservationStatus.CONFIRMED)) {
            throw new ReservationException("Aveți deja o rezervare activă. Anulați-o pe cea veche pentru a crea una nouă.");
        }

        R_Table table = tableRepository.findWithLockingById(request.getTableId())
                .orElseThrow(() -> new ResourceNotFoundException("Masa selectată nu a fost găsită."));

        LocalDateTime startTime = request.getReservationTime();
        LocalDateTime endTime = startTime.plusHours(RESERVATION_DURATION_HOURS);

        if (reservationRepository.existsOverlappingReservation(table.getId(), startTime, endTime)) {
            throw new ReservationException("Masa este deja rezervată pentru acest interval orar. Vă rugăm alegeți altă oră.");
        }

        if (request.getNumberOfGuests() > table.getCapacity() || request.getNumberOfGuests() < 1) {
            throw new ReservationException("Numărul de persoane este invalid pentru capacitatea mesei.");
        }

        Reservation reservation = Reservation.builder()
                .user(user)
                .table(table)
                .numberOfGuests(request.getNumberOfGuests())
                .reservationTime(startTime)
                .status(ReservationStatus.CONFIRMED)
                .build();
        reservationRepository.save(reservation);

        try {
            String messageBody = String.format("Rezervare noua la Barlog!\nClient: %s\nMasa: %s\nData: %s\nPersoane: %d",
                    user.getNickname() != null ? user.getNickname() : user.getFirstName(), table.getName(), startTime.toString(), request.getNumberOfGuests());

        } catch (Exception e) {
            log.error("Trimiterea notificarii prin SMS a esuat, dar rezervarea a fost creata.", e);
        }
    }

    @Transactional
    public void cancelMyReservation(User user) {
        Reservation reservation = reservationRepository.findByUserAndStatus(user, ReservationStatus.CONFIRMED)
                .orElseThrow(() -> new ResourceNotFoundException("Nu a fost găsită nicio rezervare activă pentru a fi anulată."));

        reservationRepository.delete(reservation);
    }

    public UserReservationDto getMyReservation(User user) {
        return reservationRepository.findByUserAndStatus(user, ReservationStatus.CONFIRMED)
                .map(res -> new UserReservationDto(res.getId(), res.getTable().getName(), res.getNumberOfGuests(), res.getReservationTime()))
                .orElse(null);
    }

    // --- Metode pentru Admin ---

    public List<AdminReservationViewDto> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::mapToAdminDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelReservationById(Long reservationId) {
        if (!reservationRepository.existsById(reservationId)) {
            throw new ResourceNotFoundException("Rezervarea cu ID-ul " + reservationId + " nu a fost găsită.");
        }
        reservationRepository.deleteById(reservationId);
    }

    // Metodă ajutătoare pentru DTO-ul de admin
    private AdminReservationViewDto mapToAdminDto(Reservation reservation) {
        return new AdminReservationViewDto(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getTable().getName(),
                reservation.getUser().getEmail(),
                reservation.getNumberOfGuests(),
                reservation.getReservationTime(),
                reservation.getStatus().name()
        );
    }
}