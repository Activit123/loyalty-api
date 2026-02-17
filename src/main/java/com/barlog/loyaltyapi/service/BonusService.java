package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.ActiveBonusDto;
import com.barlog.loyaltyapi.model.*;
import com.barlog.loyaltyapi.repository.UserItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BonusService {

    private final UserItemRepository userItemRepository;

    /**
     * Calculează valoarea totală a unui bonus procentual (ex: XP, COINS).
     * Returnează un multiplicator (ex: 1.0 = normal, 1.5 = +50%).
     */
    public double calculateMultiplier(User user, ItemEffectType effectType, ProductCategory category) {
        List<UserItem> equippedItems = userItemRepository.findByUserAndIsEquippedTrue(user);
        double multiplier = 1.0;

        for (UserItem ui : equippedItems) {
            for (ItemEffect effect : ui.getItemTemplate().getEffects()) {
                
                // Verificăm dacă tipul efectului corespunde
                if (effect.getEffectType() == effectType) {
                    
                    // Verificăm target-ul (dacă efectul e specific pe categorie)
                    if (effect.getTargetCategory() == null || effect.getTargetCategory() == category) {
                        // Presupunem că valoarea în DB e procentuală (ex: 10 pentru 10%)
                        multiplier += (effect.getValue() / 100.0);
                    }
                }
            }
        }
        return multiplier;
    }

    /**
     * Calculează valoarea totală a unui bonus absolut (ex: Discount fix, Reducere cerințe quest).
     * Returnează suma valorilor (ex: -5 monede).
     */
    public double calculateFlatBonus(User user, ItemEffectType effectType) {
        List<UserItem> equippedItems = userItemRepository.findByUserAndIsEquippedTrue(user);
        double totalFlatValue = 0.0;

        for (UserItem ui : equippedItems) {
            for (ItemEffect effect : ui.getItemTemplate().getEffects()) {
                if (effect.getEffectType() == effectType) {
                    totalFlatValue += effect.getValue();
                }
            }
        }
        return totalFlatValue;
    }

    public ActiveBonusDto getUserActiveBonuses(User user) {
        ActiveBonusDto dto = new ActiveBonusDto();
        List<UserItem> equippedItems = userItemRepository.findByUserAndIsEquippedTrue(user);

        for (UserItem ui : equippedItems) {
            String itemName = ui.getItemTemplate().getName();

            for (ItemEffect effect : ui.getItemTemplate().getEffects()) {
                double val = effect.getValue();
                String sourceText = itemName + " (+" + val + (effect.getEffectType().name().contains("PERCENT") || val < 100 ? "%" : "") + ")";

                switch (effect.getEffectType()) {

                    // 1. Shop Discount
                    case SHOP_DISCOUNT_GLOBAL:
                        dto.setShopDiscountPercent(dto.getShopDiscountPercent() + val);
                        dto.getShopDiscountSources().add(sourceText);
                        break;

                    // 2. XP Global
                    case XP_BOOST_GLOBAL:
                        dto.setGlobalXpMultiplier(dto.getGlobalXpMultiplier() + (val / 100.0));
                        dto.getXpBonusSources().add(sourceText);
                        break;

                    // 3. XP Categorie (Mai complex)
                    case XP_BOOST_CATEGORY:
                        if (effect.getTargetCategory() != null) {
                            String catName = effect.getTargetCategory().name();

                            // Calcul Multiplier
                            double currentMult = dto.getCategoryXpMultipliers().getOrDefault(catName, 1.0);
                            dto.getCategoryXpMultipliers().put(catName, currentMult + (val / 100.0));

                            // Adăugare Sursă
                            dto.getCategoryXpSources().computeIfAbsent(catName, k -> new ArrayList<>()).add(sourceText);
                        }
                        break;

                    // 4. Coin Boost
                    case COIN_BOOST_GLOBAL:
                        dto.setCoinMultiplier(dto.getCoinMultiplier() + (val / 100.0));
                        dto.getCoinBonusSources().add(sourceText);
                        break;

                    // 5. Quest Reward
                    case QUEST_REWARD_BOOST:
                        dto.setQuestRewardMultiplier(dto.getQuestRewardMultiplier() + (val / 100.0));
                        dto.getQuestBonusSources().add(sourceText);
                        break;

                    // 6. Passive Daily Coins
                    case PASSIVE_DAILY_COINS:
                        dto.setDailyPassiveCoins(dto.getDailyPassiveCoins() + (int) val);
                        dto.getPassiveCoinSources().add(itemName + " (+" + (int)val + " monede/zi)");
                        break;

                    // 7. Lucky Scan
                    case LUCKY_SCAN_BONUS:
                        dto.setLuckyScanChance(dto.getLuckyScanChance() + val);
                        dto.getLuckyScanSources().add(sourceText);
                        break;
                }
            }
        }

        // Limitare logică pentru discount (max 90%)
        if (dto.getShopDiscountPercent() > 90) {
            dto.setShopDiscountPercent(90);
            dto.getShopDiscountSources().add("(Limitat la 90%)");
        }

        return dto;
    }
}