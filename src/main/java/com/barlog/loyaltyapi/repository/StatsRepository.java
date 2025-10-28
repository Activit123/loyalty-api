// src/main/java/com/barlog/loyaltyapi/repository/StatsRepository.java

package com.barlog.loyaltyapi.repository;

import com.barlog.loyaltyapi.model.User; // Asigură-te că importul este corect
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StatsRepository extends JpaRepository<User, Long> {

    /**
     * Calculează numărul total de utilizatori cu rolul 'ROLE_USER'.
     * Presupunem că entitatea User are un câmp 'role'.
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'ROLE_USER'")
    long countActiveUsers();

    /**
     * Calculează numărul de tipuri de produse care sunt marcate ca fiind active.
     * Presupunem că entitatea Product are un câmp 'isActive'.
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.isActive = true")
    long countAvailableProducts();

    /**
     * ADEVĂRATA METODĂ PENTRU "MONEDE ACORDATE":
     * Calculează suma totală a tranzacțiilor pozitive (acordări de monede).
     * Presupunem că entitatea se numește 'CoinTransaction' și are câmpurile 'transactionType' și 'amount'.
     *
     * !!! IMPORTANT: Trebuie să ajustezi string-urile de mai jos ('ADMIN_AWARD', 'RECEIPT_SCAN')
     * pentru a se potrivi EXACT cu valorile pe care le salvezi în coloana 'transaction_type'.
     */
    @Query("SELECT COALESCE(SUM(ct.amount), 0) FROM CoinTransaction ct WHERE ct.transactionType IN ('ADMIN_AWARD', 'RECEIPT_SCAN', 'REWARD')")
    long sumTotalAwardedCoins();

    /**
     * Calculează numărul de anunțuri active (presupunând că ai o entitate Announcement).
     * Presupunem că entitatea Announcement are un câmp 'isActive'.
     */
    @Query("SELECT COUNT(a) FROM Announcement a")
    long countActiveAnnouncements();
}