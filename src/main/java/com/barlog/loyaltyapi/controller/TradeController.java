package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.TradeDetailsDto;
import com.barlog.loyaltyapi.dto.TradeInitiateRequest;
import com.barlog.loyaltyapi.dto.TradeOfferRequest;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.service.TradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trade")
@RequiredArgsConstructor

public class TradeController {
    
    private final TradeService tradeService;

    // 1. Inițiază un trade
    @PostMapping("/initiate")
    public ResponseEntity<TradeDetailsDto> initiateTrade(
            Authentication authentication,
            @Valid @RequestBody TradeInitiateRequest request) {
        
        User initiator = (User) authentication.getPrincipal();
        TradeDetailsDto trade = tradeService.initiateTrade(initiator, request);
        
        return new ResponseEntity<>(trade, HttpStatus.CREATED);
    }
    
    // 2. Face o ofertă (sau acceptă oferta celuilalt)
    @PostMapping("/{tradeId}/offer")
    public ResponseEntity<TradeDetailsDto> makeOffer(
            Authentication authentication,
            @PathVariable Long tradeId,
            @RequestBody TradeOfferRequest request) {
        
        User user = (User) authentication.getPrincipal();
        TradeDetailsDto trade = tradeService.makeOffer(user, tradeId, request);
        
        return ResponseEntity.ok(trade);
    }

    // 3. Finalizează trade-ul (execută mutarea itemelor și monedelor)
    @PatchMapping("/{tradeId}/complete")
    public ResponseEntity<TradeDetailsDto> completeTrade(
            Authentication authentication,
            @PathVariable Long tradeId) {
        
        User user = (User) authentication.getPrincipal();
        TradeDetailsDto trade = tradeService.completeTrade(user, tradeId);
        
        return ResponseEntity.ok(trade);
    }

    // 4. Anulează trade-ul
    @DeleteMapping("/{tradeId}")
    public ResponseEntity<Void> cancelTrade(
            Authentication authentication,
            @PathVariable Long tradeId) {
        
        User user = (User) authentication.getPrincipal();
        tradeService.cancelTrade(user, tradeId);
        
        return ResponseEntity.noContent().build();
    }
    
    // 5. Preia detaliile complete ale unui trade
    @GetMapping("/{tradeId}")
    public ResponseEntity<TradeDetailsDto> getTradeDetails(@PathVariable Long tradeId) {
        TradeDetailsDto details = tradeService.getTradeDetails(tradeId);
        return ResponseEntity.ok(details);
    }

    // 6. Preia lista de trade-uri active ale utilizatorului
    @GetMapping("/active")
    public ResponseEntity<List<TradeDetailsDto>> getActiveTrades(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<TradeDetailsDto> activeTrades = tradeService.getActiveTrades(user);
        return ResponseEntity.ok(activeTrades);
    }
}