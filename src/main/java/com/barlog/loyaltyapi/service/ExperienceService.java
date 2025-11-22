package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.model.ClassType;
import com.barlog.loyaltyapi.model.ProductCategory;
import com.barlog.loyaltyapi.model.Race;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.model.XpTransaction;
import com.barlog.loyaltyapi.repository.UserRepository;
import com.barlog.loyaltyapi.repository.XpTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    // NOUA CONSTANTA: Multiplicator pentru XP la revendicarea bonului (Buy Price * 5 / Claim Value)
    // Deoarece în cerință ai spus "ca și cum ar fi cumpărat produsul la preț de magazin, adică de 5 ori mai mult"
    private static final double XP_MULTIPLIER_FOR_RECEIPT_CLAIM = 5.0;
    private static final double XP_PER_COIN_SPENT_IN_SHOP = 1.0;

    // Metoda apelată la cumpărarea unui produs din magazin
    @Transactional
    public void addExperienceForShopPurchase(User user, double coinsSpent, ProductCategory category) {
        double baseExperience = coinsSpent * XP_PER_COIN_SPENT_IN_SHOP;
        String description = "Cumpărături din magazin în valoare de " + coinsSpent + " monede.";
        // Trecem baseExperience ca Double
        addExperience(user, baseExperience, "SHOP_PURCHASE", category, description);
    }

    // Metoda apelată la revendicarea unui bon/acordarea de monede de către admin
    @Transactional
    public void addExperienceForReceiptClaim(User user, int coinsClaimed) {

        // Logica simplificată: XP = Coins Claimed * 5.0 (conform cerinței tale)
        double baseExperience = coinsClaimed * XP_MULTIPLIER_FOR_RECEIPT_CLAIM;
        String description = "Revendicare bon fiscal: " + coinsClaimed + " monede acordate (XP Rate: x5).";

        // Folosim o categorie relevantă pentru bonusurile rasiale (Entry Fee e un bun default)
        addExperience(user, baseExperience, "RECEIPT_CLAIM", ProductCategory.ENTRY_FEE, description);
    }

    // Metoda centrală și privată care calculează și adaugă experiența (MODIFICATĂ PENTRU DOUBLE)
    protected void addExperience(User user, double baseAmount, String sourceType, ProductCategory category, String description) {
        updateConsecutiveActivityBonus(user, category);
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
        double finalExperience = modifiedAmount * user.getXpRate(); // finalExperience este de tip Double

        if (finalExperience <= 0.0) return;

        // Actualizăm totalul de XP al utilizatorului
        user.setExperience(user.getExperience() + finalExperience); // Adună Double
        userRepository.save(user);

        // Creăm o înregistrare în istoricul de XP
        XpTransaction transaction = XpTransaction.builder()
                .user(user)
                .amount(finalExperience) // amount este de tip Double în XpTransaction
                .sourceType(sourceType)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();
        xpTransactionRepository.save(transaction);
    }

    // Metodă helper pentru bonusul rasial (folosește Double)
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

    // Metodă helper pentru bonusul de clasă (folosește Double)
    private double getClassBonus(ClassType classType, ProductCategory category) {
        if (classType.getGameTypeBonusCategory() == null || category == null) return 1.0;
        return 1.0;
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
    @Transactional
    public void addManualExperience(String email, double amount) { // Am eliminat parametrul 'description'
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizatorul cu emailul " + email + " nu a fost găsit."));

        if (amount <= 0) {
            throw new IllegalArgumentException("Suma de XP trebuie să fie pozitivă.");
        }

        XpTransaction transaction = XpTransaction.builder()
                .user(user)
                .amount(amount)
                .sourceType("ADMIN_MANUAL_ADD")
                // Am setat o descriere generică pentru claritate în baza de date
                .description("XP acordat manual de către un administrator.")
                .createdAt(LocalDateTime.now())
                .build();
        xpTransactionRepository.save(transaction);

        user.setExperience(user.getExperience() + amount);
        userRepository.save(user);
    }
}