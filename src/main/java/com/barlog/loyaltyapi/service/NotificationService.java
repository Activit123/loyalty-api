package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class NotificationService {

    // 1. Stocăm conexiunile per User ID.
    // Un user poate avea mai multe conexiuni (tab-uri diferite sau telefon + pc), deci folosim o Listă.
    private final Map<Long, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    // Păstrăm și lista de admini pentru notificări globale (dacă mai ai nevoie de ea)
    private final List<SseEmitter> adminEmitters = new CopyOnWriteArrayList<>();

    /**
     * Conectează un utilizator la fluxul de notificări.
     */
    public void addUserEmitter(Long userId, SseEmitter emitter) {
        // Obține lista existentă sau creează una nouă
        userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        log.info("User {} conectat la SSE. Total conexiuni active pentru user: {}", userId, userEmitters.get(userId).size());

        // Cleanup la deconectare
        emitter.onCompletion(() -> removeUserEmitter(userId, emitter));
        emitter.onTimeout(() -> removeUserEmitter(userId, emitter));
        emitter.onError(e -> removeUserEmitter(userId, emitter));
    }

    private void removeUserEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                userEmitters.remove(userId);
            }
        }
        log.debug("Conexiune SSE închisă pentru user {}", userId);
    }

    /**
     * Trimite o notificare în timp real către un utilizator specific.
     * Aceasta este metoda pe care o vei apela din TradeService, FriendshipService, etc.
     */
    public void sendNotificationToUser(Long userId, Object notificationData) {
        List<SseEmitter> emitters = userEmitters.get(userId);

        if (emitters == null || emitters.isEmpty()) {
            log.debug("User {} nu este conectat online. Notificarea a fost salvată în DB, dar nu s-a trimis push.", userId);
            return;
        }

        // Trimite către toate dispozitivele user-ului
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification") // Numele evenimentului pe care îl ascultăm în React
                        .data(notificationData));
            } catch (IOException e) {
                // Conexiunea a murit, o ștergem
                removeUserEmitter(userId, emitter);
            }
        }
    }

    // --- Păstrăm logica veche pentru Admini (opțional) ---
    public void addAdminEmitter(SseEmitter emitter) {
        adminEmitters.add(emitter);
        emitter.onCompletion(() -> adminEmitters.remove(emitter));
        emitter.onTimeout(() -> adminEmitters.remove(emitter));
    }

    // Trimite la toți adminii (ex: pentru dashboard)
    public void sendToAllAdmins(String eventName, Object data) {
        for (SseEmitter emitter : adminEmitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                adminEmitters.remove(emitter);
            }
        }
    }
}