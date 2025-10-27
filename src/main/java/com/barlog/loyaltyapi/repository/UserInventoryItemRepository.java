package com.barlog.loyaltyapi.repository;

import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.model.UserInventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserInventoryItemRepository extends JpaRepository<UserInventoryItem, Long> {
    // Găsește itemele active din inventarul unui utilizator
    List<UserInventoryItem> findByUserAndStatus(User user, String status);

    // Găsește un item după codul unic de revendicare (UUID)
    Optional<UserInventoryItem> findByClaimUid(UUID claimUid);
}