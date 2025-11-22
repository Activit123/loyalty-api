package com.barlog.loyaltyapi.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "friendships", uniqueConstraints = {
        // Asigură că nu pot exista două înregistrări identice (UserA, UserB)
        // Nota: Ordinea este importantă. Vom impune o ordine canonică în Service.
        @UniqueConstraint(columnNames = {"user_a_id", "user_b_id"})
})
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Utilizatorul A (cel cu ID-ul mai mic, pentru ordine canonică)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_a_id", nullable = false)
    private User userA;

    // Utilizatorul B (cel cu ID-ul mai mare)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_b_id", nullable = false)
    private User userB;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status;
    // NOU: ID-ul utilizatorului care a trimis cererea (NU este obligatoriu userA)
    @Column(name = "initiator_id", nullable = false)
    private Long initiatorId;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}