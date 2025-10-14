package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.service.AIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.barlog.loyaltyapi.dto.ReceiptResponseDto;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;
    private final ObjectMapper objectMapper; // Spring injectează automat un ObjectMapper

    @PostMapping(value = "/scan-receipt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()") // Orice utilizator logat poate accesa acest endpoint
    public ResponseEntity<?> scanReceipt(@RequestPart("image") MultipartFile imageFile) {
        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest().body("Imaginea nu poate fi goală.");
        }
        try {
            String jsonData = aiService.extractReceiptData(imageFile);
            // Validăm și convertim string-ul JSON într-un obiect DTO
            ReceiptResponseDto receiptDto = objectMapper.readValue(jsonData, ReceiptResponseDto.class);
            return ResponseEntity.ok(receiptDto);
        } catch (Exception e) {
            e.printStackTrace(); // Util pentru debugging în consolă
            return ResponseEntity.status(500).body("Eroare la procesarea imaginii: " + e.getMessage());
        }
    }
}