package com.barlog.loyaltyapi.model;

public enum ItemEffectType {
    // Economie & Farming
    XP_BOOST_GLOBAL,        // Crește XP la orice acțiune
    XP_BOOST_CATEGORY,      // Crește XP doar la o categorie specifică
    COIN_BOOST_GLOBAL,      // Crește monedele primite
    
    // Magazin Virtual
    SHOP_DISCOUNT_GLOBAL,   // Reducere preț iteme virtuale

    // Gameplay
    QUEST_REWARD_BOOST,
    PASSIVE_DAILY_COINS,    // Generează monede zilnic (valoare absolută)
    LUCKY_SCAN_BONUS  // Bonus la recompensele din Quest-uri
}