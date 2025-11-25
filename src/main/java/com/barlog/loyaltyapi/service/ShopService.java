package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.MatchedProductDto;
import com.barlog.loyaltyapi.dto.ReceiptProductDto;
import com.barlog.loyaltyapi.dto.ReceiptResponseDto;
import com.barlog.loyaltyapi.model.*;
import com.barlog.loyaltyapi.repository.*;
import info.debatty.java.stringsimilarity.JaroWinkler;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CoinTransactionRepository coinTransactionRepository;
    private final ShopPurchaseRepository shopPurchaseRepository;
    private final ExperienceService experienceService;
    private final UserInventoryItemRepository userInventoryItemRepository; // Injectează noul repo
    private final QuestService questService;
    private final UserNotificationService notificationService; // INJECTAT
    @Transactional
    public User purchaseProduct(Long productId, User currentUser) {
        // 1. Găsim produsul și verificăm dacă este valid
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Produsul nu a fost găsit."));

        if (!product.isActive()) {
            throw new IllegalStateException("Acest produs nu mai este disponibil.");
        }
        if (product.getStock() == 0) {
            throw new IllegalStateException("Stoc epuizat pentru acest produs.");
        }

        // 2. Verificăm dacă utilizatorul are suficiente fonduri
        if (currentUser.getCoins() < product.getBuyPrice()) {
            throw new IllegalStateException("Fonduri insuficiente pentru a cumpăra acest produs.");
        }

        // NOUA VALIDARE PENTRU PREȚ NEGATIV
        if (product.getBuyPrice() < 0) {
            throw new IllegalStateException("Eroare de configurare: Prețul produsului nu poate fi negativ.");
        }

        // 3. Procesăm tranzacția
        currentUser.setCoins(currentUser.getCoins() - product.getBuyPrice());

        // Decrementăm stocul dacă nu este nelimitat
        if (product.getStock() != -1) {
            product.setStock(product.getStock() - 1);
        }

        // 4. Creăm înregistrări în istoric
        CoinTransaction transaction = CoinTransaction.builder()
                .user(currentUser)
                .amount(-product.getBuyPrice())
                .description("Cumpărat produs: " + product.getName())
                .transactionType("SHOP_PURCHASE")
                .createdAt(LocalDateTime.now())
                .build();
        coinTransactionRepository.save(transaction);

        ShopPurchase purchase = ShopPurchase.builder()
                .user(currentUser)
                .product(product)
                .costAtPurchase(product.getBuyPrice())
                .purchasedAt(LocalDateTime.now())
                .build();
        ShopPurchase savedPurchase = shopPurchaseRepository.save(purchase);
        UserInventoryItem newItem = UserInventoryItem.builder()
                .user(currentUser)
                .product(product)
                // NOU: Itemele cumpărate trebuie să aibă purchase_id
                .purchase(savedPurchase)
                .status("IN_INVENTORY")
                .build();
        userInventoryItemRepository.save(newItem);

        // Salvăm entitățile modificate
        productRepository.save(product);
        experienceService.addExperienceForShopPurchase(currentUser, product.getBuyPrice(), product.getCategory());

        // ELIMINAT: Apelul questService.updateQuestProgress(...)

        return userRepository.save(currentUser);
    }

    public List<MatchedProductDto> matchReceiptItems(ReceiptResponseDto receiptData) {
        // 1. Preluăm toate produsele active din magazinul nostru
        List<Product> allShopProducts = productRepository.findAll(); // Poți adăuga o metodă findByIsActive(true)
        if (allShopProducts.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. Inițializăm algoritmul de comparare a string-urilor
        JaroWinkler jw = new JaroWinkler();
        final double SIMILARITY_THRESHOLD = 0.7; // Prag de similaritate (ajustează-l după teste)

        List<MatchedProductDto> matchedProducts = new ArrayList<>();

        // 3. Iterăm prin fiecare item extras de AI de pe bon
        for (ReceiptProductDto receiptItem : receiptData.getProduse()) {
            if (receiptItem.getDescriere() == null) continue;

            Product bestMatch = null;
            double highestSimilarity = 0.0;

            // 4. Comparăm item-ul de pe bon cu fiecare produs din magazin
            for (Product shopProduct : allShopProducts) {
                double similarity = jw.similarity(
                        receiptItem.getDescriere().toLowerCase(),
                        shopProduct.getName().toLowerCase()
                );

                if (similarity > highestSimilarity) {
                    highestSimilarity = similarity;
                    bestMatch = shopProduct;
                }
            }

            // 5. Dacă cea mai bună potrivire depășește pragul, o considerăm validă
            if (highestSimilarity >= SIMILARITY_THRESHOLD) {
                matchedProducts.add(new MatchedProductDto(receiptItem, bestMatch));
            } else {
                // Opțional: putem adăuga și item-urile care nu au fost găsite
                matchedProducts.add(new MatchedProductDto(receiptItem, null));
            }
        }

        return matchedProducts;
    }

}