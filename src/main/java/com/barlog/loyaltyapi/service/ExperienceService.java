package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.model.*;
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

    private static final int XP_PER_COIN_SPENT_IN_SHOP = 1;
    private static final int XP_PER_COIN_CLAIMED_FROM_RECEIPT = 2;

    @Transactional
    public void addExperienceForShopPurchase(User user, int coinsSpent, ProductCategory category) {
        int baseExperience = coinsSpent * XP_PER_COIN_SPENT_IN_SHOP;
        String description = "Cumpărături din magazin în valoare de " + coinsSpent + " monede.";
        addExperience(user, baseExperience, "SHOP_PURCHASE", category, description);
    }

    @Transactional
    public void addExperienceForReceiptClaim(User user, int coinsClaimed) {
        int baseExperience = coinsClaimed * XP_PER_COIN_CLAIMED_FROM_RECEIPT;
        String description = "XP pentru bon fiscal în valoare de " + coinsClaimed + " monede.";
        addExperience(user, baseExperience, "RECEIPT_CLAIM", ProductCategory.ENTRY_FEE, description);
    }

    private void addExperience(User user, int baseAmount, String sourceType, ProductCategory category, String description) {
        double modifiedAmount = baseAmount;

        if (user.getRace() != null) {
            modifiedAmount *= getRacialBonus(user.getRace(), category);
        }
        if (user.getClassType() != null) {
            modifiedAmount *= getClassBonus(user.getClassType(), category);
        }

        // Aplică bonusul de login (x2, x4) care a fost setat în UserService
        int finalExperience = (int) Math.round(modifiedAmount * user.getXpRate());

        if (finalExperience <= 0) return;

        user.setExperience(user.getExperience() + finalExperience);
        userRepository.save(user);

        XpTransaction transaction = XpTransaction.builder()
                .user(user)
                .amount(finalExperience)
                .sourceType(sourceType)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();
        xpTransactionRepository.save(transaction);
    }

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

    private double getClassBonus(ClassType classType, ProductCategory category) {
        return 1.0;
    }
}