package com.barlog.loyaltyapi.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "tournaments")
public class Tournament {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String gameName;
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private LocalDateTime startTime;
    private Integer maxPlayers;
    private Integer entryFeeCoins;
    
    private String prizeDescription;
    private Integer prizeCoins;
    
    private String status;
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}