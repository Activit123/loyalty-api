package com.barlog.loyaltyapi.repository;

import com.barlog.loyaltyapi.model.Trade;
import com.barlog.loyaltyapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    // Găsește tranzacțiile active (care nu sunt COMPLETED sau CANCELED)
    @Query("SELECT t FROM Trade t WHERE (t.initiator = ?1 OR t.recipient = ?1) AND t.status IN ('INITIATED', 'PENDING_APPROVAL', 'ACCEPTED') ORDER BY t.createdAt DESC")
    List<Trade> findActiveTradesByUser(User user);

    // Interogare corectă (3 parametri: user, inventoryItemId, currentTradeId)
    @Query("SELECT t FROM Trade t JOIN TradeOfferItem toi ON t.id = toi.trade.id WHERE toi.inventoryItem.id = ?2 AND (t.initiator = ?1 OR t.recipient = ?1) AND t.status IN ('INITIATED', 'PENDING_APPROVAL', 'ACCEPTED') AND t.id <> ?3")
    Optional<Trade> findActiveTradeByItemAndUser(User user, Long inventoryItemId, Long currentTradeId);
}