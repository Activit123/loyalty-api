package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.NotificationDto;
import com.barlog.loyaltyapi.model.Notification;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.service.SseService;
import com.barlog.loyaltyapi.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
public class UserNotificationController {

    private final UserNotificationService userNotificationService;
    private final SseService sseService;

    // 1. Subscriere la Fluxul Live
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // Timeout Infinit (sau foarte mare)
        
        sseService.addUserEmitter(user.getId(), emitter);
        
        try {
            emitter.send(SseEmitter.event().name("init").data("Conectat"));
        } catch (Exception e) {}
        
        return emitter;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getMyNotifications(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Notification> notifications = userNotificationService.getUserNotifications(user);

        // Mapează la DTO
        List<NotificationDto> dtos = notifications.stream().map(n -> NotificationDto.builder()
                .id(n.getId())
                .message(n.getMessage())
                .type(n.getType())
                .link(n.getLink())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build()).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // 3. Număr Necitite
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(userNotificationService.getUnreadCount(user));
    }

    // 4. Marchează una ca citită
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(Authentication authentication, @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        userNotificationService.markAsRead(user, id);
        return ResponseEntity.ok().build();
    }

    // 5. Marchează toate ca citite
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        userNotificationService.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }
}