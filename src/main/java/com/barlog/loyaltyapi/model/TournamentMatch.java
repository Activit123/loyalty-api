package com.barlog.loyaltyapi.model;

import jakarta.persistence.*;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "tournament_matches")
public class TournamentMatch {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player1_id")
    private User player1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player2_id")
    private User player2;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "winner_id")
    private User winner;

    private Integer roundNumber;
    private Integer matchOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_match_id")
    private TournamentMatch nextMatch;
}