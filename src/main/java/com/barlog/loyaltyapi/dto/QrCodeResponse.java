package com.barlog.loyaltyapi.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class QrCodeResponse {
    private UUID code; // Acesta va fi transformat în QR
    private String itemName;
    private String qrUrl; // Link-ul complet (ex: app://claim/UUID)
}