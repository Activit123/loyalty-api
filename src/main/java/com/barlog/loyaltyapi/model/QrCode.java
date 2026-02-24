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
@Table(name = "qr_codes")
public class QrCode {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_template_id", nullable = false)
    private ItemTemplate itemTemplate;

    @Column(name = "is_used", nullable = false)
    @Builder.Default
    private boolean isUsed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_by_user_id")
    private User usedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}