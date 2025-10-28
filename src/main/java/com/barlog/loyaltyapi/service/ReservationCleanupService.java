package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.model.Reservation;
import com.barlog.loyaltyapi.model.ReservationStatus;
import com.barlog.loyaltyapi.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationCleanupService {
    private final ReservationRepository reservationRepository;
    private static final Logger log = LoggerFactory.getLogger(ReservationCleanupService.class);
    private static final int RESERVATION_DURATION_HOURS = 2; // Durata standard

    /**
     * Rulează automat la fiecare oră. Caută rezervările care s-au încheiat
     * și le marchează ca 'COMPLETED'.
     */
    @Scheduled(fixedRate = 3600000) // 1 oră
    @Transactional
    public void completePastReservations() {
        log.info("Rulează job-ul de curățare a rezervărilor finalizate...");
        LocalDateTime now = LocalDateTime.now();

        // Găsim rezervările confirmate a căror perioadă s-a încheiat
        // (ora de start + durata rezervării este în trecut)
        List<Reservation> pastReservations = reservationRepository.findAllByStatusAndReservationTimeBefore(
                ReservationStatus.CONFIRMED,
                now.minusHours(RESERVATION_DURATION_HOURS)
        );

        if (pastReservations.isEmpty()) {
            log.info("Nicio rezervare de finalizat.");
            return;
        }

        log.info("Au fost găsite {} rezervări de marcat ca finalizate.", pastReservations.size());
        for (Reservation reservation : pastReservations) {
            reservation.setStatus(ReservationStatus.COMPLETED);
            reservationRepository.save(reservation);
            log.info("Rezervarea ID {} a fost marcată ca finalizată.", reservation.getId());
        }
    }
}