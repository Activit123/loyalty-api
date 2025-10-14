package com.barlog.loyaltyapi.repository;

import com.barlog.loyaltyapi.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    // Găsește toate anunțurile, sortate de la cel mai nou la cel mai vechi
    List<Announcement> findAllByOrderByCreatedAtDesc();
}