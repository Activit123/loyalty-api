package com.barlog.loyaltyapi.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_inventory_items")
public class UserInventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = true, unique = true)
    private ShopPurchase purchase;

    @Column(nullable = false)
    private String status;

    @Column(name = "claim_uid", nullable = false, unique = true)
    private UUID claimUid;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        claimUid = UUID.randomUUID(); // Generează automat un UUID la creare
    }
}