package com.barlog.loyaltyapi.service;

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
public class SseService {

    // Mapare: UserID -> Listă de conexiuni (poate fi logat pe telefon și laptop)
    private final Map<Long, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    public void addUserEmitter(Long userId, SseEmitter emitter) {
        userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        
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
    }

    public void sendNotificationToUser(Long userId, Object data) {
        List<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null) {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().name("notification").data(data));
                } catch (IOException e) {
                    removeUserEmitter(userId, emitter);
                }
            }
        }
    }
}