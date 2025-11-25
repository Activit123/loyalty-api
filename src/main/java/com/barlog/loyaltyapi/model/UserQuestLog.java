package com.barlog.loyaltyapi.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_quest_log")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserQuestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "completion_date")
    private LocalDateTime completionDate;

    @OneToMany(mappedBy = "userQuestLog", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserCriterionProgress> criterionProgress = new HashSet<>();
}