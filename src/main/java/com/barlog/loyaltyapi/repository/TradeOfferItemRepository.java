package com.barlog.loyaltyapi.repository;

import com.barlog.loyaltyapi.dto.TradeOfferItem;
import com.barlog.loyaltyapi.model.Trade;
import com.barlog.loyaltyapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeOfferItemRepository extends JpaRepository<TradeOfferItem, Long> {
    
    List<TradeOfferItem> findByTrade(Trade trade);
    
    // Șterge toate itemele oferite de un utilizator într-un anumit trade
    @Modifying
    @Query("DELETE FROM TradeOfferItem t WHERE t.trade = ?1 AND t.user = ?2")
    void deleteByTradeAndUser(Trade trade, User user);
    
    // Găsește toate itemele oferite de un utilizator (pentru a calcula suma de monede)
    List<TradeOfferItem> findByTradeAndUser(Trade trade, User user);
}