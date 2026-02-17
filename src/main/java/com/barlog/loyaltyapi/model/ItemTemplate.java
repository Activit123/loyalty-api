package com.barlog.loyaltyapi.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "item_templates")
public class ItemTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;
    
    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemSlot slot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemRarity rarity;

    @Column(name = "min_level")
    private Integer minLevel;

    @Column(name = "buy_price", nullable = false)
    private Integer buyPrice;

    @Column(name = "sell_price")
    private Integer sellPrice;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;

    // Relația One-to-Many cu Efectele (Cascade ALL pentru a salva efectele odată cu itemul)
    @OneToMany(mappedBy = "itemTemplate", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<ItemEffect> effects = new ArrayList<>();


    @Column(name = "req_str")
    private Integer reqStr = 0;

    @Column(name = "req_dex")
    private Integer reqDex = 0;

    @Column(name = "req_int")
    private Integer reqInt = 0;

    @Column(name = "req_cha")
    private Integer reqCha = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}