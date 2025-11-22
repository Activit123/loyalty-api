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
@Table(name = "quest_criteria")
public class QuestCriterion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest; 

    @Enumerated(EnumType.STRING)
    @Column(name = "criterion_type", nullable = false)
    private QuestType criterionType; 

    @Enumerated(EnumType.STRING)
    @Column(name = "target_category") 
    private ProductCategory targetCategory; 
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_product_id")
    private Product targetProduct; 

    @Column(name = "required_amount", nullable = false)
    private Double requiredAmount; 
}