package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.CreateTableRequestDto;
import com.barlog.loyaltyapi.dto.TableDto;
import com.barlog.loyaltyapi.model.R_Table;
import com.barlog.loyaltyapi.model.ReservationStatus;
import com.barlog.loyaltyapi.repository.ReservationRepository;
import com.barlog.loyaltyapi.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableService {
    private final TableRepository tableRepository;
    private final ReservationRepository reservationRepository;
    private static final int RESERVATION_DURATION_HOURS = 2;

    @Transactional
    public TableDto createTable(CreateTableRequestDto request) {
        R_Table newTable = R_Table.builder().name(request.getName()).capacity(request.getCapacity()).build();
        R_Table savedTable = tableRepository.save(newTable);
        return new TableDto(savedTable.getId(), savedTable.getName(), savedTable.getCapacity(), "AVAILABLE"); // Default status for DTO
    }

    public List<TableDto> getAllTablesWithStatusForDateTime(LocalDateTime dateTime) {
        List<R_Table> allTables = tableRepository.findAllByOrderByIdAsc();
        LocalDateTime endTime = dateTime.plusHours(RESERVATION_DURATION_HOURS);

        // Optimizare: Preluăm toate ID-urile meselor rezervate în acea zi o singură dată
        Set<Long> reservedTableIdsInInterval = reservationRepository
                .findByStatusAndReservationTimeBetween(ReservationStatus.CONFIRMED, dateTime.toLocalDate().atStartOfDay(), dateTime.toLocalDate().atTime(23, 59, 59))
                .stream()
                // Filtrăm din nou în memorie pentru suprapunerea exactă a orelor
                .filter(res -> dateTime.isBefore(res.getReservationTime().plusHours(RESERVATION_DURATION_HOURS)) && res.getReservationTime().isBefore(endTime))
                .map(res -> res.getTable().getId())
                .collect(Collectors.toSet());

        return allTables.stream().map(table -> new TableDto(
                table.getId(),
                table.getName(),
                table.getCapacity(),
                reservedTableIdsInInterval.contains(table.getId()) ? "RESERVED" : "AVAILABLE"
        )).collect(Collectors.toList());
    }
}