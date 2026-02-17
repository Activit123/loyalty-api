package com.barlog.loyaltyapi.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet; // Important
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quests")
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // 1. Hash doar pe câmpurile marcate
public class Quest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // 2. Include doar ID-ul în Hash
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestType type;

    @Column(name = "reward_coins")
    private Integer rewardCoins;

    @Column(name = "reward_xp")
    private Double rewardXp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_product_id")
    private Product rewardProduct;
    // --- NOU ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_item_template_id")
    private ItemTemplate rewardItemTemplate;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // NU FOLOSIM @Data, deci nu avem bucla aici.
    @OneToMany(mappedBy = "quest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // Important pentru Builder
    private Set<QuestCriterion> criteria = new HashSet<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}