package com.barlog.loyaltyapi.repository;

import com.barlog.loyaltyapi.model.ItemSlot;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.model.UserItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserItemRepository extends JpaRepository<UserItem, Long> {
    
    // Găsește toate itemele utilizatorului (Inventarul)
    List<UserItem> findByUser(User user);

    // Găsește doar itemele echipate (pentru calcul bonusuri)
    List<UserItem> findByUserAndIsEquippedTrue(User user);

    // Verifică dacă userul deține deja acest template (pentru a evita duplicate la iteme unice, dacă va fi cazul)
    boolean existsByUserAndItemTemplateId(User user, Long itemTemplateId);

    // Găsește itemul echipat pe un anumit slot (ex: Ce are userul pe HEAD?)
    // Aceasta este crucială pentru funcția de "Swap" (schimbare echipament)
    @Query("SELECT ui FROM UserItem ui WHERE ui.user = :user AND ui.isEquipped = true AND ui.itemTemplate.slot = :slot")
    Optional<UserItem> findEquippedItemBySlot(@Param("user") User user, @Param("slot") ItemSlot slot);
}