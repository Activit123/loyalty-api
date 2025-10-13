package com.barlog.loyaltyapi.service;
import com.barlog.loyaltyapi.dto.*;
import com.barlog.loyaltyapi.model.R_Table;
import com.barlog.loyaltyapi.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
@Service @RequiredArgsConstructor
public class TableService {
    private final TableRepository tableRepository;
    @Transactional
    public TableDto createTable(CreateTableRequestDto request) {
        R_Table newTable = R_Table.builder().name(request.getName()).capacity(request.getCapacity()).build();
        return mapToDto(tableRepository.save(newTable));
    }
    public List<TableDto> getAllTables() {
        return tableRepository.findAllByOrderByIdAsc().stream().map(this::mapToDto).collect(Collectors.toList());
    }
    private TableDto mapToDto(R_Table table) {
        return new TableDto(table.getId(), table.getName(), table.getCapacity(), table.getStatus());
    }
}