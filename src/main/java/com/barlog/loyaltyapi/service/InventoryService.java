package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.InventoryItemDto;
import com.barlog.loyaltyapi.model.CoinTransaction;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.model.UserInventoryItem;
import com.barlog.loyaltyapi.repository.CoinTransactionRepository;
import com.barlog.loyaltyapi.repository.UserInventoryItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final UserInventoryItemRepository inventoryRepository;
    private final CoinTransactionRepository coinTransactionRepository;
    private final ProductService productService; // Pentru mapare

    // Metodă pentru a afișa inventarul unui user
    public List<InventoryItemDto> getMyInventory(User user) {
        return inventoryRepository.findByUserAndStatus(user, "IN_INVENTORY").stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Metoda centrală pentru revendicarea unui item de către admin
    @Transactional
    public InventoryItemDto claimItem(UUID claimUid) {
        UserInventoryItem item = inventoryRepository.findByClaimUid(claimUid)
                .orElseThrow(() -> new EntityNotFoundException("Cod de revendicare invalid sau deja folosit."));

        if (!"IN_INVENTORY".equals(item.getStatus())) {
            throw new IllegalStateException("Acest item a fost deja revendicat.");
        }

        // Actualizăm statusul item-ului
        item.setStatus("CLAIMED");
        item.setClaimedAt(LocalDateTime.now());
        UserInventoryItem claimedItem = inventoryRepository.save(item);

        // Creăm o tranzacție de monede cu valoare 0 pentru istoric
        CoinTransaction transaction = CoinTransaction.builder()
                .user(item.getUser())
                .amount(0)
                .description("Revendicat item: " + item.getProduct().getName())
                .transactionType("ITEM_CLAIM")
                .createdAt(LocalDateTime.now())
                .build();
        coinTransactionRepository.save(transaction);
        
        return mapToDto(claimedItem);
    }

    // Metodă ajutătoare de mapare
    public InventoryItemDto mapToDto(UserInventoryItem item) {
        InventoryItemDto dto = new InventoryItemDto();
        dto.setId(item.getId());
        dto.setClaimUid(item.getClaimUid());
        dto.setStatus(item.getStatus());
        // Folosim productService pentru a mapa consistent produsul
        dto.setProduct(productService.mapToDto(item.getProduct()));
        return dto;
    }
}