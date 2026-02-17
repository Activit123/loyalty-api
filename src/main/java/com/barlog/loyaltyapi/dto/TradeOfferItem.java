package com.barlog.loyaltyapi.dto;

import com.barlog.loyaltyapi.model.*;
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

    // --- 1. LEGĂTURA CU PĂRINTELE (Trade-ul mare) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id", nullable = false)
    private Trade trade;

    // --- 2. CINE OFERĂ (Eu sau Celălalt) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // --- 3. CE TIP E? (Monedă / Produs Fizic / Item RPG) ---
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private TradeOfferItemType itemType;

    // =========================================================
    // AICI SUNT "SLOTURILE" PENTRU FIECARE TIP DE OBIECT
    // Doar unul dintre acestea va fi completat, restul vor fi NULL
    // =========================================================

    // A. Dacă e Monedă
    @Column(name = "offered_amount")
    private Integer offeredAmount;

    // B. Dacă e Produs Fizic (din UserInventoryItem - ex: Suc, Chips)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id")
    private UserInventoryItem inventoryItem;

    // C. Dacă e Item RPG (din UserItem - ex: Sabie, Inel) -> ASTA E NOUTATEA
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_item_id")
    private UserItem userItem;
}