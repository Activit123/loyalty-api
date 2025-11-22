package com.barlog.loyaltyapi.dto;

import com.barlog.loyaltyapi.model.Trade;
import com.barlog.loyaltyapi.model.TradeOfferItemType;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.model.UserInventoryItem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "trade_offer_items")
public class TradeOfferItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referință la tranzacția principală
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id", nullable = false)
    private Trade trade;

    // Utilizatorul care oferă itemul/monedele
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private TradeOfferItemType itemType;

    // Suma de monede (dacă itemType = COINS)
    @Column(name = "offered_amount")
    private Integer offeredAmount; // Poate fi null dacă e un item

    // Referință la Itemul din Inventar (dacă itemType = INVENTORY_ITEM)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id")
    private UserInventoryItem inventoryItem;
}