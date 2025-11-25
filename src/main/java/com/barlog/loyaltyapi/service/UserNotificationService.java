package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.NotificationDto;
import com.barlog.loyaltyapi.exception.ResourceNotFoundException;
import com.barlog.loyaltyapi.model.Notification;
import com.barlog.loyaltyapi.model.NotificationType;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserNotificationService {

    private final NotificationRepository notificationRepository;
    private final SseService sseService;

    @Transactional
    public void notifyUser(User user, String message, NotificationType type, String link) {
        // 1. Salvează în DB
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .type(type)
                .link(link)
                .isRead(false)
                .build();
        Notification saved = notificationRepository.save(notification);

        // 2. Trimite DTO prin SSE (NU entitatea!)
        NotificationDto dto = NotificationDto.builder()
                .id(saved.getId())
                .message(saved.getMessage())
                .type(saved.getType())
                .link(saved.getLink())
                .isRead(saved.isRead())
                .createdAt(saved.getCreatedAt())
                .build();

        sseService.sendNotificationToUser(user.getId(), dto);
    }
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUser(user, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Transactional
    public void markAsRead(User user, Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificare negăsită"));
        if (!n.getUser().equals(user)) throw new IllegalStateException("Acces interzis");
        n.setRead(true);
        notificationRepository.save(n);
    }

    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unread = notificationRepository.findByUser(user, Sort.unsorted())
                .stream().filter(n -> !n.isRead()).toList();
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}