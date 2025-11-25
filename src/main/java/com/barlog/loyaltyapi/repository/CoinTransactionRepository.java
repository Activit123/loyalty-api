package com.barlog.loyaltyapi.repository;

import com.barlog.loyaltyapi.model.CoinTransaction;
import com.barlog.loyaltyapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CoinTransactionRepository extends JpaRepository<CoinTransaction, Long> {

    List<CoinTransaction> findAllByOrderByCreatedAtDesc();
    @Query("SELECT ct FROM CoinTransaction ct WHERE ct.user = :user AND ct.createdAt > :startTime AND (LOWER(ct.description) LIKE 'revendicare:%' OR LOWER(ct.description) LIKE 'cumpărat produs:%')")
    List<CoinTransaction> findPhysicalPurchasesByUserAfterDate(@Param("user") User user, @Param("startTime") LocalDateTime startTime);

    @Query("SELECT ct FROM CoinTransaction ct WHERE ct.user = :user AND ct.createdAt > :startTime AND ct.amount > 0 AND LOWER(ct.description) NOT LIKE 'revendicare:%' AND LOWER(ct.description) NOT LIKE 'cumpărat produs:%'")
    List<CoinTransaction> findNetCoinGainsByUserAfterDate(@Param("user") User user, @Param("startTime") LocalDateTime startTime);
}

