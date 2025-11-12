// src/main/java/com/barlog/loyaltyapi/model/MenuItem.java
package com.barlog.loyaltyapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "menu_items")
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description; // Poate fi folosit pentru arome de ceai sau detalii bere

    @Column(nullable = false)
    private Integer price; // Prețul în lei

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MenuItemCategory category;

    @Column(nullable = true)
    private String volume; // Ex: "250 ml", "300 g"

    @Column(nullable = true)
    private String icon; // Ex: "🥤", "☕", sau un nume de icon (dacă folosești librării)

    @Column(nullable = false)
    private Integer orderInMenu; // Pentru a sorta produsele în meniu

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true; // Pentru a activa/dezactiva un item din meniu
}