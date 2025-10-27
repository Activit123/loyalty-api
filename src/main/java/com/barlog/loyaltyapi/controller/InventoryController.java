package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.InventoryItemDto;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @GetMapping("/me")
    public ResponseEntity<List<InventoryItemDto>> getMyInventory(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(inventoryService.getMyInventory(currentUser));
    }
}