    package com.barlog.loyaltyapi.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ActiveBonusDto {

    // 1. SHOP DISCOUNT (Reducere la cumpărare iteme virtuale)
    private double shopDiscountPercent = 0.0;
    private List<String> shopDiscountSources = new ArrayList<>();

    // 2. XP BOOST GLOBAL (La orice acțiune)
    private double globalXpMultiplier = 1.0; // Base 1.0
    private List<String> xpBonusSources = new ArrayList<>();

    // 3. XP BOOST CATEGORIE (Specific pe tip produs: BERE, SUC etc.)
    // Key: Nume Categorie (ex: "BEER"), Value: Multiplicator (ex: 1.2)
    private Map<String, Double> categoryXpMultipliers = new HashMap<>(); 
    // Key: Nume Categorie, Value: Lista surse
    private Map<String, List<String>> categoryXpSources = new HashMap<>();

    // 4. COIN BOOST (La revendicare bonuri)
    private double coinMultiplier = 1.0;
    private List<String> coinBonusSources = new ArrayList<>();

    // 5. QUEST REWARD BOOST (La finalizare quest)
    private double questRewardMultiplier = 1.0;
    private List<String> questBonusSources = new ArrayList<>();

    // 6. PASSIVE DAILY COINS (Venit pasiv)
    private int dailyPassiveCoins = 0;
    private List<String> passiveCoinSources = new ArrayList<>();

    // 7. LUCKY SCAN BONUS (Șansă la extra monedă)
    private double luckyScanChance = 0.0; // Procent (ex: 5.0)
    private List<String> luckyScanSources = new ArrayList<>();
}