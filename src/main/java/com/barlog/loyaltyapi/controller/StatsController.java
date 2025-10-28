// src/main/java/com/barlog/loyaltyapi/controller/StatsController.java

package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.DashboardStatsDTO;
import com.barlog.loyaltyapi.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin") // Toate rutele din acest controller vor începe cu /api/admin
public class StatsController {

    private final StatsService statsService;

    @Autowired
    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')") // Securizează endpoint-ul! Doar adminii îl pot accesa.
    public ResponseEntity<DashboardStatsDTO> getDashboardStatistics() {
        DashboardStatsDTO stats = statsService.getDashboardStats();
        return ResponseEntity.ok(stats); // Returnează DTO-ul cu status 200 OK
    }
}