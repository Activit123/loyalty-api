package com.barlog.loyaltyapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore; // <--- IMPORT IMPORTANT
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "item_effects")
public class ItemEffect {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_template_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore // <--- ADAUGĂ ASTA! Asta oprește bucla infinită.
    private ItemTemplate itemTemplate;

    @Enumerated(EnumType.STRING)
    @Column(name = "effect_type", nullable = false)
    private ItemEffectType effectType;

    @Column(name = "effect_value", nullable = false)
    private Double value;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_category")
    private ProductCategory targetCategory;
}