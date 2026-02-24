package com.barlog.loyaltyapi.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "tournament_participants")
public class TournamentParticipant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime joinedAt;

    @PrePersist
    protected void onCreate() { joinedAt = LocalDateTime.now(); }
}