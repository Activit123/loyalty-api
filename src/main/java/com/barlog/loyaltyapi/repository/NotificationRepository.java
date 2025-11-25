package com.barlog.loyaltyapi.repository;

import com.barlog.loyaltyapi.model.Notification;
import com.barlog.loyaltyapi.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Returnează notificările sortate (cele mai noi primele)
    List<Notification> findByUser(User user, Sort sort);
    
    // Numără notificările necitite pentru bulina roșie
    long countByUserAndIsReadFalse(User user);
}