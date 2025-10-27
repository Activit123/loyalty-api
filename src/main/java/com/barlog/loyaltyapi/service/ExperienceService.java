package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.model.ClassType;
import com.barlog.loyaltyapi.model.ProductCategory;
import com.barlog.loyaltyapi.model.Race;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.model.XpTransaction;
import com.barlog.loyaltyapi.repository.UserRepository;
import com.barlog.loyaltyapi.repository.XpTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ExperienceService {

    private final UserRepository userRepository;
    private final XpTransactionRepository xpTransactionRepository;

    // Constante pentru a defini valorile de bază ale experienței
    private static final int XP_PER_COIN_SPENT_IN_SHOP = 1;
    private static final int XP_PER_COIN_CLAIMED_FROM_RECEIPT = 2; // Mai valoros
    private static final int BASE_XP_FOR_ENTRY_FEE = 10; // XP de bază pentru o intrare

    // Metoda apelată la cumpărarea unui produs din magazin
    @Transactional
    public void addExperienceForShopPurchase(User user, int coinsSpent, ProductCategory category) {
        int baseExperience = coinsSpent * XP_PER_COIN_SPENT_IN_SHOP;
        String description = "Cumpărături din magazin în valoare de " + coinsSpent + " monede.";
        addExperience(user, baseExperience, "SHOP_PURCHASE", category, description);
    }

    // Metoda apelată la revendicarea unui bon/acordarea de monede de către admin
    @Transactional
    public void addExperienceForReceiptClaim(User user, int coinsClaimed) {
        // Presupunem că 10 monede acordate = 1 intrare
        int numberOfEntries = coinsClaimed / 10;
        if (numberOfEntries <= 0) return; // Nu acordăm XP pentru sume mici

        int baseExperience = numberOfEntries * BASE_XP_FOR_ENTRY_FEE;
        String description = "Acordare " + coinsClaimed + " monede (echivalent " + numberOfEntries + " intrări).";
        addExperience(user, baseExperience, "RECEIPT_CLAIM", ProductCategory.ENTRY_FEE, description);
    }

    // Metoda centrală și privată care calculează și adaugă experiența
    private void addExperience(User user, int baseAmount, String sourceType, ProductCategory category, String description) {
        double modifiedAmount = baseAmount;

        // 1. Aplică Bonusul Rasial (dacă există)
        if (user.getRace() != null) {
            modifiedAmount *= getRacialBonus(user.getRace(), category);
        }

        // 2. Aplică Bonusul de Clasă (dacă există)
        if (user.getClassType() != null) {
            modifiedAmount *= getClassBonus(user.getClassType(), category);
        }

        // 3. Aplică Rata Generală de XP
        int finalExperience = (int) Math.round(modifiedAmount * user.getXpRate());

        if (finalExperience <= 0) return;

        // Actualizăm totalul de XP al utilizatorului
        user.setExperience(user.getExperience() + finalExperience);
        userRepository.save(user);

        // Creăm o înregistrare în istoricul de XP
        XpTransaction transaction = XpTransaction.builder()
                .user(user)
                .amount(finalExperience)
                .sourceType(sourceType)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();
        xpTransactionRepository.save(transaction);
    }

    // Metodă helper pentru bonusul rasial
    private double getRacialBonus(Race race, ProductCategory category) {
        if (race.getLoyaltyBonusCategory() == null || category == null) return 1.0;

        String bonusCategory = race.getLoyaltyBonusCategory();

        switch (bonusCategory) {
            case "SUBSCRIPTION":
                if (category == ProductCategory.SUBSCRIPTION) return race.getLoyaltyBonusXpMultiplier();
                break;
            case "ENERGY_DRINK":
                if (Set.of(ProductCategory.ENERGY_DRINK, ProductCategory.SOFT_DRINK).contains(category)) return race.getLoyaltyBonusXpMultiplier();
                break;
            case "SNACKS_SALT":
                if (Set.of(ProductCategory.SNACKS_SALT, ProductCategory.BEER).contains(category)) return race.getLoyaltyBonusXpMultiplier();
                break;
            case "ENTRY_FEE":
                if (category == ProductCategory.ENTRY_FEE) return race.getLoyaltyBonusXpMultiplier();
                break;
            default:
                return 1.0;
        }
        return 1.0;
    }

    // Metodă helper pentru bonusul de clasă
    private double getClassBonus(ClassType classType, ProductCategory category) {
        if (classType.getGameTypeBonusCategory() == null || category == null) return 1.0;

        // Presupunem că bonusul de clasă se aplică doar la un tip special de "eveniment"
        // pe care îl vom adăuga în viitor, nu la cumpărături.
        // Aici poți adăuga logica: ex, dacă sourceType este "GAME_SESSION" și categoria se potrivește.
        // Pentru moment, lăsăm un placeholder.
        //
        // Exemplu:
        // if (sourceType.equals("GAME_SESSION") && category.name().equals(classType.getGameTypeBonusCategory())) {
        //     return classType.getGameTypeXpMultiplier();
        // }

        return 1.0;
    }
}