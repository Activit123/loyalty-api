// Sursa: src/main/java/com/barlog/loyaltyapi/repository/UserQuestLogRepository.java

package com.barlog.loyaltyapi.repository;

import com.barlog.loyaltyapi.model.QuestStatus;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.model.UserQuestLog;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // Import necesar
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserQuestLogRepository extends JpaRepository<UserQuestLog, Long> {



    // CORECTAT: Interogare JPQL/HQL cu @Param și JOIN FETCH pe criterii (pentru a evita N+1)
    // Ne asigurăm că progresul este încărcat (care este o colecție pe UserQuestLog)
    Optional<UserQuestLog> findByUserAndQuestIdAndStatus(User user, Long questId, QuestStatus status);

    // MODIFICATĂ: Interogarea simplă (fără JOIN FETCH pe Progres)
    @Query("SELECT ql FROM UserQuestLog ql " +
            "JOIN FETCH ql.quest q " + // Încărcăm Quest-ul
            "LEFT JOIN FETCH q.criteria qc " + // Încărcăm Criteriile Quest-ului
            "WHERE ql.user = :user AND ql.status IN ('ACTIVE', 'COMPLETED', 'REWARDED')")
    List<UserQuestLog> findUserQuestsWithDetails(@Param("user") User user);
   

    // CORECTAT: Schimbăm denumirea metodei.
    @Query("SELECT ql FROM UserQuestLog ql " +
            "JOIN FETCH ql.quest q " +
            "LEFT JOIN FETCH q.criteria qc " +
            "LEFT JOIN FETCH ql.criterionProgress p " + // Adaugăm progresul utilizatorului
            "WHERE ql.user = :user AND ql.status IN ('ACTIVE', 'COMPLETED', 'REWARDED')")
    List<UserQuestLog> findAllQuestsWithProgressByUserId(@Param("user") User user);

    // CORECTAT: Am eliminat JOIN FETCH pe ql.criterionProgress (colecția de tip List de pe UserQuestLog)
    @Query("SELECT DISTINCT ql FROM UserQuestLog ql " +
            "JOIN FETCH ql.quest q " +
            "LEFT JOIN FETCH q.criteria qc " +
            "LEFT JOIN FETCH ql.criterionProgress p " + // Acum e Set
            "WHERE ql.user = :user AND ql.status IN ('ACTIVE', 'COMPLETED', 'REWARDED')")
    List<UserQuestLog> findAllByUserAndStatusInForDisplay(@Param("user") User user);
}