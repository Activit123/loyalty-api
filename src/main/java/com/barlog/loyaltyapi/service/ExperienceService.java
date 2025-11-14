package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.model.*;
import com.barlog.loyaltyapi.repository.UserRepository;
import com.barlog.loyaltyapi.repository.XpTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
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
        // Presupunem că revendicarea de monede se face la locație, deci este considerată ENTRY_FEE
        addExperience(user, baseExperience, "RECEIPT_CLAIM", ProductCategory.ENTRY_FEE, description);
    }

    private void addExperience(User user, int baseAmount, String sourceType, ProductCategory category, String description) {
        updateConsecutiveActivityBonus(user, category);
        double modifiedAmount = baseAmount;

        if (user.getRace() != null) {
            modifiedAmount *= getRacialBonus(user.getRace(), category);
        }
        if (user.getClassType() != null) {
            modifiedAmount *= getClassBonus(user.getClassType(), category);
        }

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

    @Transactional
    public void updateConsecutiveActivityBonus(User user, ProductCategory category) {
        if (category != ProductCategory.ENTRY_FEE) {
            return;
        }

        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();

        if (dayOfWeek == DayOfWeek.MONDAY || dayOfWeek == DayOfWeek.TUESDAY) {
            return;
        }

        LocalDate lastActivity = user.getLastActivityDate();
        if (lastActivity != null && lastActivity.isEqual(today)) {
            return;
        }

        int consecutiveDays = user.getConsecutiveActivityDays() != null ? user.getConsecutiveActivityDays() : 0;

        // ======================= ÎNCEPUTUL MODIFICĂRII =======================
        // Verificăm dacă ciclul anterior s-a încheiat (a ajuns la 8 zile)
        if (consecutiveDays >= 8) {
            // Dacă da, resetăm la 1 pentru a începe un nou ciclu
            user.setConsecutiveActivityDays(1);
        } else {
            // Altfel, continuăm logica normală de incrementare sau resetare a seriei
            boolean isConsecutiveAfterWeekend = (lastActivity != null && lastActivity.getDayOfWeek() == DayOfWeek.SUNDAY && today.getDayOfWeek() == DayOfWeek.WEDNESDAY && today.minusDays(3).isEqual(lastActivity));

            if ((lastActivity != null && lastActivity.isEqual(today.minusDays(1))) || isConsecutiveAfterWeekend) {
                user.setConsecutiveActivityDays(consecutiveDays + 1);
            } else {
                user.setConsecutiveActivityDays(1);
            }
        }
        // ======================== SFÂRȘITUL MODIFICĂRII ========================

        // Logica de setare a ratei de XP rămâne aceeași, se va baza pe noua valoare calculată mai sus
        if (user.getConsecutiveActivityDays() >= 8) {
            user.setXpRate(4.0);
        } else if (user.getConsecutiveActivityDays() >= 4) {
            user.setXpRate(2.0);
        } else {
            user.setXpRate(1.0);
        }

        user.setLastActivityDate(today);
        // Nu mai este nevoie de userRepository.save(user) aici, deoarece se face în addExperience
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