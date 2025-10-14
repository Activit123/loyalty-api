package com.barlog.loyaltyapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class NotificationService {

    // O listă thread-safe pentru a stoca conexiunile active ale adminilor
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /**
     * Adaugă un nou SseEmitter (o nouă conexiune de la un admin) la listă.
     */
    public void addEmitter(SseEmitter emitter) {
        this.emitters.add(emitter);
        log.info("Admin nou conectat pentru notificări. Total admini conectați: {}", emitters.size());

        // La deconectare sau eroare, îl eliminăm din listă
        emitter.onCompletion(() -> removeEmitter(emitter));
        emitter.onTimeout(() -> removeEmitter(emitter));
        emitter.onError(e -> removeEmitter(emitter));
    }

    /**
     * Elimină un SseEmitter din listă.
     */
    private void removeEmitter(SseEmitter emitter) {
        this.emitters.remove(emitter);
        log.info("Admin deconectat. Total admini conectați: {}", emitters.size());
    }

    /**
     * Trimite o notificare către toți adminii conectați.
     * @param eventName Numele evenimentului (ex: "new_reservation")
     * @param data Datele de trimis (pot fi un String sau un obiect JSON)
     */
    public void sendNotificationToAllAdmins(String eventName, Object data) {
        log.info("Se trimite notificarea '{}' către {} admini...", eventName, emitters.size());

        for (SseEmitter emitter : emitters) {
            try {
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .name(eventName)
                        .data(data);
                emitter.send(event);
            } catch (IOException e) {
                log.error("Eroare la trimiterea notificării către un admin. Se elimină conexiunea.", e);
                // Dacă trimiterea eșuează, înseamnă că clientul s-a deconectat. Îl eliminăm.
                removeEmitter(emitter);
            }
        }
    }
}