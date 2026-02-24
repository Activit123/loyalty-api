package com.barlog.loyaltyapi.dto;

import lombok.Data;
import java.util.List;

@Data
public class TradeOfferRequest {
    // Oferta utilizatorului curent
    private Integer offeredCoins;
    private List<Long> offeredInventoryItemIds;
    private List<Long> offeredUserItemIds; // Iteme RPG
    // Utilizatorul curent acceptă oferta celuilalt?
    private boolean acceptsFinalOffer;

}