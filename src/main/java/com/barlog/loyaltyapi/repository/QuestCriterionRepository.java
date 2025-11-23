package com.barlog.loyaltyapi.repository;

import com.barlog.loyaltyapi.model.QuestCriterion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestCriterionRepository extends JpaRepository<QuestCriterion, Long> {
    // Rămâne simplu
}