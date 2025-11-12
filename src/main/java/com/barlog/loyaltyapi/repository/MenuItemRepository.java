// src/main/java/com/barlog/loyaltyapi/repository/MenuItemRepository.java
package com.barlog.loyaltyapi.repository;

import com.barlog.loyaltyapi.model.MenuItem;
import com.barlog.loyaltyapi.model.MenuItemCategory; // Asigură-te că MenuItemCategory este importat
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    // Pentru afișarea publică a meniului
    List<MenuItem> findByIsActiveTrueOrderByCategoryAscOrderInMenuAsc();

    // Pentru administrare (include și inactive)
    List<MenuItem> findAllByOrderByCategoryAscOrderInMenuAsc();

    // Poți adăuga și metode de căutare după categorie dacă e necesar
    List<MenuItem> findByIsActiveTrueAndCategoryOrderByOrderInMenuAsc(MenuItemCategory category);
}