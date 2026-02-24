package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.QrCodeListDto;
import com.barlog.loyaltyapi.dto.QrCodeResponse;
import com.barlog.loyaltyapi.exception.ResourceNotFoundException;
import com.barlog.loyaltyapi.model.*;
import com.barlog.loyaltyapi.repository.ItemTemplateRepository;
import com.barlog.loyaltyapi.repository.QrCodeRepository;
import com.barlog.loyaltyapi.repository.UserItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QrCodeService {

    private final QrCodeRepository qrCodeRepository;
    private final ItemTemplateRepository itemTemplateRepository;
    private final UserItemRepository userItemRepository;
    private final UserNotificationService notificationService;

    // --- ADMIN: Generează un cod unic pentru un item ---
    public List<QrCodeListDto> getAllLootCodes() {
        return qrCodeRepository.findAll().stream()
                .map(qr -> QrCodeListDto.builder()
                        .id(qr.getId())
                        .itemName(qr.getItemTemplate() != null ? qr.getItemTemplate().getName() : "Item Necunoscut")
                        .createdAt(qr.getCreatedAt())
                        .isUsed(qr.isUsed())
                        .usedByEmail(qr.getUsedBy() != null ? qr.getUsedBy().getEmail() : null)
                        .usedAt(qr.getUsedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public QrCodeResponse generateQrForLoot(Long itemTemplateId) {
        ItemTemplate item = itemTemplateRepository.findById(itemTemplateId)
                .orElseThrow(() -> new ResourceNotFoundException("Item-ul nu există."));

        QrCode qrCode = QrCode.builder()
                .itemTemplate(item)
                .isUsed(false)
                .build();
        
        qrCode = qrCodeRepository.save(qrCode);

        return QrCodeResponse.builder()
                .code(qrCode.getId())
                .itemName(item.getName())
                // Acesta este string-ul pe care îl va conține QR-ul
                .qrUrl(qrCode.getId().toString()) 
                .build();
    }

    // --- USER: Scanează și revendică Loot-ul ---
    @Transactional
    public String claimLoot(User user, String codeStr) {
        UUID code;
        try {
            code = UUID.fromString(codeStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Cod QR invalid.");
        }

        QrCode qrEntry = qrCodeRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("Codul QR nu a fost găsit în sistem."));

        if (qrEntry.isUsed()) {
            throw new IllegalStateException("Acest cufăr a fost deja deschis de altcineva!");
        }

        // 1. Dăm itemul utilizatorului
        UserItem newItem = UserItem.builder()
                .user(user)
                .itemTemplate(qrEntry.getItemTemplate())
                .isEquipped(false)
                .build();
        userItemRepository.save(newItem);

        // 2. Marcăm codul ca folosit
        qrEntry.setUsed(true);
        qrEntry.setUsedBy(user);
        qrEntry.setUsedAt(LocalDateTime.now());
        qrCodeRepository.save(qrEntry);

        // 3. Notificare
        notificationService.notifyUser(user, "Ai găsit un Loot ascuns: " + qrEntry.getItemTemplate().getName() + "!", NotificationType.SYSTEM, "/character");

        return "Felicitări! Ai obținut: " + qrEntry.getItemTemplate().getName();
    }
}