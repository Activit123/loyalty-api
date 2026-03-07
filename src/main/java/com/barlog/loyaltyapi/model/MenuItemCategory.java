package com.barlog.loyaltyapi.model;

import lombok.Getter;

@Getter
public enum MenuItemCategory {
    SOFT_DRINKS("Răcoritoare"),
    COFFEE_TEA("Cafea & Ceai"),
    ENERGY_DRINKS("Energizante"),
    SNACKS("Snackuri & Ronțăieli"),
    CRAFT_BEER("Bere Craft"),
    WINE("Vin"); // <-- Adăugat
    private final String displayName;

    MenuItemCategory(String displayName) {
        this.displayName = displayName;
    }

}
