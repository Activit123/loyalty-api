package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.ClaimRequest1DTO;
import com.barlog.loyaltyapi.dto.ClaimRequestDto;
import com.barlog.loyaltyapi.dto.InventoryItemDto;
import com.barlog.loyaltyapi.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminInventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/claim")
    public ResponseEntity<InventoryItemDto> claimItem(@Valid @RequestBody ClaimRequest1DTO request) {
        InventoryItemDto claimedItem = inventoryService.claimItem(request.getClaimUid());
        return ResponseEntity.ok(claimedItem);
    }
}