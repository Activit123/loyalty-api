package com.barlog.loyaltyapi.repository;

import com.barlog.loyaltyapi.model.ShopPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopPurchaseRepository extends JpaRepository<ShopPurchase, Long> {}