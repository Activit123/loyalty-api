package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.TransactionDetailsDto;
import com.barlog.loyaltyapi.dto.UserLeaderboardDto;
import com.barlog.loyaltyapi.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reporting") // Am ales un URL sugestiv
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Securizăm toate endpoint-urile din controller
public class AdminReportingController {

    private final AdminService adminService;

    /**
     * Endpoint pentru a afișa top 10 utilizatori după numărul de "coins".
     * URL: GET /api/admin/reporting/leaderboard
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<UserLeaderboardDto>> getLeaderboard() {
        List<UserLeaderboardDto> leaderboard = adminService.getLeaderboard();
        return ResponseEntity.ok(leaderboard);
    }

    /**
     * Endpoint pentru a vedea istoricul tuturor tranzacțiilor din sistem.
     * URL: GET /api/admin/reporting/transactions/global
     */
    @GetMapping("/transactions/global")
    public ResponseEntity<List<TransactionDetailsDto>> getGlobalTransactions() {
        List<TransactionDetailsDto> transactions = adminService.getGlobalTransactions();
        return ResponseEntity.ok(transactions);
    }
}