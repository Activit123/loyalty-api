package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.model.R_Table;
import com.barlog.loyaltyapi.model.Reservation;
import com.barlog.loyaltyapi.model.ReservationStatus;
import com.barlog.loyaltyapi.model.TableStatus;
import com.barlog.loyaltyapi.repository.ReservationRepository;
import com.barlog.loyaltyapi.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationCleanupService {
    private final ReservationRepository reservationRepository;
    private final TableRepository tableRepository;

    /**
     * Acest job rulează automat la fiecare oră (3,600,000 milisecunde).
     * El caută rezervările care au trecut și le marchează ca fiind 'COMPLETED',
     * eliberând în același timp mesele asociate.
     */
    @Scheduled(fixedRate = 3600000) // Rulează la fiecare oră
    @Transactional
    public void completePastReservations() {
        log.info("Rulează job-ul de finalizare a rezervărilor trecute...");

        LocalDateTime now = LocalDateTime.now();

        // Găsim toate rezervările confirmate a căror oră a trecut.
        // Adăugăm o marjă (ex: 2 ore) pentru a considera durata unei rezervări.
        List<Reservation> pastReservations = reservationRepository.findAllByStatusAndReservationTimeBefore(
                ReservationStatus.CONFIRMED,
                now.minusHours(2) // Considerăm o rezervare "trecută" la 2 ore după ora ei
        );

        if (pastReservations.isEmpty()) {
            log.info("Nicio rezervare de finalizat.");
            return;
        }

        log.info("Au fost găsite {} rezervări trecute pentru a fi finalizate.", pastReservations.size());

        for (Reservation reservation : pastReservations) {
            // 1. Actualizăm statusul rezervării
            reservation.setStatus(ReservationStatus.COMPLETED);
            reservationRepository.save(reservation);

            // 2. Eliberăm masa asociată
            R_Table table = reservation.getTable();
            if (table != null) {
                table.setStatus(TableStatus.AVAILABLE);
                tableRepository.save(table);
                log.info("Masa {} a fost eliberată. Rezervarea {} a fost marcată ca finalizată.", table.getName(), reservation.getId());
            }
        }
    }
}