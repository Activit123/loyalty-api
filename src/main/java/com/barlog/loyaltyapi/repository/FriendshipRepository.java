package com.barlog.loyaltyapi.repository;

import com.barlog.loyaltyapi.model.Friendship;
import com.barlog.loyaltyapi.model.FriendshipStatus;
import com.barlog.loyaltyapi.model.Trade;
import com.barlog.loyaltyapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

     // 1. Găsește o relație existentă între două conturi (folosind ordinea canonică A<B)
    Optional<Friendship> findByUserAAndUserB(User userA, User userB);

    // 2. Găsește toate prieteniile ACCEPTED sau BLOCKED pentru un utilizator (indiferent de A sau B)
    @Query("SELECT f FROM Friendship f WHERE (f.userA = :user OR f.userB = :user) AND (f.status = 'ACCEPTED' OR f.status = 'BLOCKED')")
    List<Friendship> findPermanentFriendships(@Param("user") User user);

    // 3. Găsește toate cererile PENDING primite (unde userul curent NU este initiatorId)
    // Deoarece nu putem folosi NOT EQUAL pe initiatorId în Query Method, le luăm pe ambele și filtrăm în Service.
    @Query("SELECT f FROM Friendship f WHERE (f.userA = :user OR f.userB = :user) AND f.status = 'PENDING'")
    List<Friendship> findAllPendingFriendships(@Param("user") User user);
    // Modifică interogarea pentru a accepta TradeID-ul curent și a-l EXCLUDE
    @Query("SELECT t FROM Trade t JOIN TradeOfferItem toi ON t.id = toi.trade.id WHERE toi.inventoryItem.id = ?2 AND (t.initiator = ?1 OR t.recipient = ?1) AND t.status IN ('INITIATED', 'PENDING_APPROVAL', 'ACCEPTED') AND t.id <> ?3")
    Optional<Trade> findActiveTradeByItemAndUser(User user, Long inventoryItemId, Long currentTradeId);
}