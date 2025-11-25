package com.barlog.loyaltyapi.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_criterion_progress")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserCriterionProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_id", nullable = false)
    private UserQuestLog userQuestLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criterion_id", nullable = false)
    private QuestCriterion criterion;

    @Column(nullable = false)
    private Double currentProgress = 0.0;

    @Column(nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "unique_key", nullable = false, unique = true)
    private String uniqueKey;
}