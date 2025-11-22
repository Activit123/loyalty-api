package com.barlog.loyaltyapi.repository;
import com.barlog.loyaltyapi.model.Quest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestRepository extends JpaRepository<Quest, Long> {
    List<Quest> findAllByIsActiveTrue();
    @Query("SELECT q FROM Quest q LEFT JOIN FETCH q.criteria c LEFT JOIN FETCH q.rewardProduct rp")
    List<Quest> findAllWithCriteria();
}