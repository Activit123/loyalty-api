package com.barlog.loyaltyapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "xp_transactions")
public class XpTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(nullable = false) private Double amount;
    @Column(name = "source_type", nullable = false) private String sourceType;
    private String description;
    @Column(name = "created_at") private LocalDateTime createdAt;
}