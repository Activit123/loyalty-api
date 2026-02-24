package com.barlog.loyaltyapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class TradeInitiateRequest {
    @NotBlank(message = "Recipient identifier cannot be blank")
    private String recipientIdentifier; // Email, Nickname, sau QR code value
    
    // Oferta inițială a inițiatorului (opțional)
    private Integer offeredCoins;
    private List<Long> offeredInventoryItemIds;
    private List<Long> offeredUserItemIds; // Iteme RPG (Săbii, etc)
}