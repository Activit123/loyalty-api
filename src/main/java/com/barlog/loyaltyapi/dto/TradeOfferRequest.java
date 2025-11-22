package com.barlog.loyaltyapi.dto;

import lombok.Data;
import java.util.List;

@Data
public class TradeOfferRequest {
    // Oferta utilizatorului curent
    private Integer offeredCoins;
    private List<Long> offeredInventoryItemIds;
    
    // Utilizatorul curent acceptă oferta celuilalt?
    private boolean acceptsFinalOffer;
}