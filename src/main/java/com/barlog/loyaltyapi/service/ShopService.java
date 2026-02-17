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

    private final BonusService bonusService;
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

        // --- 2. CALCUL PREȚ FINAL (CU DISCOUNT) ---
        // Întrebăm BonusService cât discount are userul (de la Iteme + Clasă)
        double totalDiscountPercent = bonusService.calculateFlatBonus(currentUser, ItemEffectType.SHOP_DISCOUNT_GLOBAL);

        // Limităm discount-ul la maxim 90% (ca să nu fie gratis sau negativ)
        if (totalDiscountPercent > 90) totalDiscountPercent = 90;

        // Calculăm prețul redus
        int originalPrice = product.getBuyPrice();
        int finalPrice = (int) Math.round(originalPrice * (1 - (totalDiscountPercent / 100.0)));

        // Verificăm prețul să nu fie negativ (just in case)
        if (finalPrice < 0) finalPrice = 0;

        // --- 3. VERIFICARE FONDURI PE PREȚUL REDUS ---
        if (currentUser.getCoins() < finalPrice) {
            throw new IllegalStateException("Fonduri insuficiente pentru a cumpăra acest produs. Cost: " + finalPrice);
        }

        // --- 4. PROCESARE TRANZACȚIE ---
        currentUser.setCoins(currentUser.getCoins() - finalPrice);

        // Decrementăm stocul dacă nu este nelimitat
        if (product.getStock() != -1) {
            product.setStock(product.getStock() - 1);
        }

        // Creăm descrierea pentru istoric (menționăm reducerea dacă există)
        String description = "Cumpărat produs: " + product.getName();
        if (finalPrice < originalPrice) {
            description += String.format(" (Redus: %d -> %d)", originalPrice, finalPrice);
        }

        // Creăm înregistrări în istoric cu PREȚUL FINAL
        CoinTransaction transaction = CoinTransaction.builder()
                .user(currentUser)
                .amount(-finalPrice) // Scădem suma redusă
                .description(description)
                .transactionType("SHOP_PURCHASE")
                .createdAt(LocalDateTime.now())
                .build();
        coinTransactionRepository.save(transaction);

        ShopPurchase purchase = ShopPurchase.builder()
                .user(currentUser)
                .product(product)
                .costAtPurchase(finalPrice) // Salvăm cât a costat efectiv
                .purchasedAt(LocalDateTime.now())
                .build();
        ShopPurchase savedPurchase = shopPurchaseRepository.save(purchase);

        UserInventoryItem newItem = UserInventoryItem.builder()
                .user(currentUser)
                .product(product)
                .purchase(savedPurchase)
                .status("IN_INVENTORY")
                .build();
        userInventoryItemRepository.save(newItem);

        // Salvăm entitățile modificate
        productRepository.save(product);

        // La XP, de obicei se dă pe baza prețului întreg sau redus?
        // De regulă, la loialitate primești XP pe cât ai cheltuit efectiv.
        experienceService.addExperienceForShopPurchase(currentUser, finalPrice, product.getCategory());

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