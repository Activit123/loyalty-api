package com.barlog.loyaltyapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_criterion_progress")
public class UserCriterionProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    // Cheia unică pentru a asigura că nu avem duplicate la inițiere
    @Column(name = "unique_key", nullable = false)
    private String uniqueKey; 
    
    @PrePersist
    private void setUniqueKey() {
        // Această logică va fi mutată în Service, dar o păstrăm ca referință
        // pentru a ne asigura că key-ul există înainte de save.
        if (this.userQuestLog != null && this.criterion != null) {
             this.uniqueKey = this.userQuestLog.getId() + "_" + this.criterion.getId();
        } else if (this.uniqueKey == null) {
             this.uniqueKey = java.util.UUID.randomUUID().toString(); // Fallback
        }
    }
}