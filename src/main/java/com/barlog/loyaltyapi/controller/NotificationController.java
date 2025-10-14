package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public SseEmitter subscribeToNotifications() {
        // Creăm un SseEmitter cu un timeout foarte lung (practic, fără timeout)
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // Adăugăm emitter-ul la serviciul de notificări
        notificationService.addEmitter(emitter);

        // Putem trimite un eveniment inițial de confirmare, dacă dorim
        try {
            emitter.send(SseEmitter.event().name("INIT").data("Conectat la fluxul de notificări."));
        } catch (Exception e) {
            // Ignorăm erorile la trimiterea inițială
        }

        return emitter;
    }
}