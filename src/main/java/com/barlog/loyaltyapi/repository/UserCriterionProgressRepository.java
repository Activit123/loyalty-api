package com.barlog.loyaltyapi.repository;

import com.barlog.loyaltyapi.model.QuestCriterion;
import com.barlog.loyaltyapi.model.UserCriterionProgress;
import com.barlog.loyaltyapi.model.UserQuestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCriterionProgressRepository extends JpaRepository<UserCriterionProgress, Long> {
    
    // Preia toate progresurile ACTIVE pentru un Quest Log specific
    List<UserCriterionProgress> findByUserQuestLogAndIsCompletedFalse(UserQuestLog log);
    void deleteByCriterion(QuestCriterion criterion);
}