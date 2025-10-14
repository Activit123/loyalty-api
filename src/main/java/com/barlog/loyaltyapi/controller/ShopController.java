package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.MatchedProductDto;
import com.barlog.loyaltyapi.dto.ReceiptResponseDto;
import com.barlog.loyaltyapi.dto.UserResponseDto;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.service.AdminService;
import com.barlog.loyaltyapi.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;
    private final AdminService adminService; // Reutilizăm metoda de mapare

    @PostMapping("/purchase/{productId}")
    public ResponseEntity<UserResponseDto> purchaseProduct(
            @PathVariable Long productId,
            Authentication authentication) {
        
        User currentUser = (User) authentication.getPrincipal();
        User updatedUser = shopService.purchaseProduct(productId, currentUser);
        
        // Returnăm datele actualizate ale utilizatorului
        return ResponseEntity.ok(adminService.mapUserToDto(updatedUser));
    }

    @PostMapping("/match-receipt")
    public ResponseEntity<List<MatchedProductDto>> matchReceipt(@RequestBody ReceiptResponseDto receiptData) {
        List<MatchedProductDto> matchedProducts = shopService.matchReceiptItems(receiptData);
        return ResponseEntity.ok(matchedProducts);
    }
}