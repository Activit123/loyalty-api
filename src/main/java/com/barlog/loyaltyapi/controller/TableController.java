package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.*;
import com.barlog.loyaltyapi.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class TableController {
    private final TableService tableService;

    @GetMapping
    public ResponseEntity<List<TableDto>> getAllTables(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime) {
        return ResponseEntity.ok(tableService.getAllTablesWithStatusForDateTime(dateTime));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TableDto> createTable(@RequestBody CreateTableRequestDto request) {
        TableDto newTable = tableService.createTable(request);
        URI location = URI.create("/api/tables/" + newTable.getId());
        return ResponseEntity.created(location).body(newTable);
    }
}