package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.AdminCreateReservationRequestDto; // Nume standardizat
import com.barlog.loyaltyapi.dto.AdminReservationViewDto;
import com.barlog.loyaltyapi.dto.CreateReservationRequest;
import com.barlog.loyaltyapi.dto.UserReservationDto;
import com.barlog.loyaltyapi.exception.ReservationException;
import com.barlog.loyaltyapi.exception.ResourceNotFoundException;
import com.barlog.loyaltyapi.model.*;
import com.barlog.loyaltyapi.repository.ReservationRepository;
import com.barlog.loyaltyapi.repository.TableRepository;
import com.barlog.loyaltyapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository; // Dependința adăugată
    private final TableRepository tableRepository;

    /**
     * Creează o rezervare pentru utilizatorul autentificat.
     * @param user Utilizatorul autentificat, injectat de Spring Security.
     * @param request Detaliile rezervării (fără userId).
     */
    @Transactional
    public void createReservation(User user, CreateReservationRequest request) { // Primește obiectul User

        if (reservationRepository.existsByUserIdAndStatus(user.getId(), ReservationStatus.CONFIRMED)) {
            throw new ReservationException("Aveți deja o rezervare activă. Anulați-o pe cea veche pentru a crea una nouă.");
        }

        R_Table table = tableRepository.findById(request.getTableId()) // Folosim 'Table'
                .orElseThrow(() -> new ResourceNotFoundException("Masa cu ID-ul " + request.getTableId() + " nu a fost găsită."));

        if (table.getStatus() != TableStatus.AVAILABLE) {
            throw new ReservationException("Masa selectată este deja ocupată.");
        }
        if (request.getNumberOfGuests() > table.getCapacity()) {
            throw new ReservationException("Numărul de persoane (" + request.getNumberOfGuests() + ") depășește capacitatea mesei (" + table.getCapacity() + ").");
        }

        table.setStatus(TableStatus.RESERVED);
        tableRepository.save(table);

        Reservation reservation = Reservation.builder()
                .user(user) // Folosim user-ul primit ca parametru
                .table(table)
                .numberOfGuests(request.getNumberOfGuests())
                .reservationTime(request.getReservationTime())
                .status(ReservationStatus.CONFIRMED)
                .build();
        reservationRepository.save(reservation);
    }

    /**
     * Creează o rezervare în numele unui utilizator, acțiune realizată de un admin.
     */
    @Transactional
    public AdminReservationViewDto createReservationByAdmin(AdminCreateReservationRequestDto request) { // Nume DTO standardizat
        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul cu email-ul " + request.getUserEmail() + " nu a fost găsit."));

        if (reservationRepository.existsByUserIdAndStatus(user.getId(), ReservationStatus.CONFIRMED)) {
            throw new ReservationException("Utilizatorul " + user.getEmail() + " are deja o rezervare activă.");
        }

        R_Table table = tableRepository.findById(request.getTableId()) // Folosim 'Table'
                .orElseThrow(() -> new ResourceNotFoundException("Masa cu ID-ul " + request.getTableId() + " nu a fost găsită."));

        if (table.getStatus() != TableStatus.AVAILABLE) {
            throw new ReservationException("Masa selectată este deja ocupată.");
        }
        if (request.getNumberOfGuests() > table.getCapacity()) {
            throw new ReservationException("Numărul de persoane depășește capacitatea mesei.");
        }

        table.setStatus(TableStatus.RESERVED);
        tableRepository.save(table);

        Reservation reservation = Reservation.builder()
                .user(user)
                .table(table)
                .numberOfGuests(request.getNumberOfGuests())
                .reservationTime(request.getReservationTime())
                .status(ReservationStatus.CONFIRMED)
                .build();
        Reservation savedReservation = reservationRepository.save(reservation);
        return mapToAdminReservationDto(savedReservation);
    }

    public UserReservationDto getMyReservation(User user) {
        return reservationRepository.findByUserIdAndStatus(user.getId(), ReservationStatus.CONFIRMED)
                .map(res -> new UserReservationDto(res.getId(), res.getTable().getName(), res.getNumberOfGuests(), res.getReservationTime()))
                .orElse(null);
    }

    @Transactional
    public void cancelMyReservation(User user) {
        Reservation reservation = reservationRepository.findByUserIdAndStatus(user.getId(), ReservationStatus.CONFIRMED)
                .orElseThrow(() -> new ResourceNotFoundException("Nu a fost găsită nicio rezervare activă pentru a fi anulată."));

        R_Table table = reservation.getTable(); // Folosim 'Table'
        table.setStatus(TableStatus.AVAILABLE);
        tableRepository.save(table);

        reservationRepository.delete(reservation);
    }

    public List<AdminReservationViewDto> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::mapToAdminReservationDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelReservationById(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Rezervarea cu ID-ul " + reservationId + " nu a fost găsită."));

        R_Table table = reservation.getTable(); // Folosim 'Table'
        table.setStatus(TableStatus.AVAILABLE);
        tableRepository.save(table);

        reservationRepository.deleteById(reservationId);
    }

    private AdminReservationViewDto mapToAdminReservationDto(Reservation reservation) {
        return new AdminReservationViewDto(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getUser().getEmail(),
                reservation.getTable().getName(),
                reservation.getNumberOfGuests(),
                reservation.getReservationTime(),
                reservation.getStatus().name()
        );
    }
}