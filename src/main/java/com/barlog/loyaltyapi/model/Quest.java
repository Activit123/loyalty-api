package com.barlog.loyaltyapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quests")
public class Quest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    
    // Recompense
    @Column(name = "reward_coins")
    private Integer rewardCoins;
    
    @Column(name = "reward_xp")
    private Double rewardXp;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_product_id")
    private Product rewardProduct; 
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // Relație LAZY cu Criteriile
    @OneToMany(mappedBy = "quest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestCriterion> criteria; 
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}