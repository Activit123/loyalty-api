package com.barlog.loyaltyapi.dto;

import com.barlog.loyaltyapi.model.TradeOfferItemType;
import lombok.Data;

// Reprezintă un singur item (sau suma de monede) dintr-o ofertă
@Data
public class TradeOfferItemDto {
    private Long id;
    private TradeOfferItemType itemType;
    private Integer offeredAmount; // Număr de monede
    private InventoryItemDto inventoryItem; // Item complet (dacă e cazul)
}