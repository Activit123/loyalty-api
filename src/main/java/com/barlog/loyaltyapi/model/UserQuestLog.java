package com.barlog.loyaltyapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_quest_log")
public class UserQuestLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestStatus status; // ACTIVE, COMPLETED, REWARDED

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "completion_date")
    private LocalDateTime completionDate;
    
    // Relație cu progresul pentru fiecare criteriu
    @OneToMany(mappedBy = "userQuestLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCriterionProgress> criterionProgress; 
}