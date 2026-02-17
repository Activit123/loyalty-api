package com.barlog.loyaltyapi.model;
import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "races")
public class Race {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String name;
    @Column(columnDefinition = "TEXT") private String description;
    private String primaryAttribute;
    @Column(columnDefinition = "TEXT") private String racialBenefit;
    private String loyaltyBonusCategory;
    private Double loyaltyBonusXpMultiplier;

    @Column(name = "base_str")
    private Integer baseStr;

    @Column(name = "base_dex")
    private Integer baseDex;

    @Column(name = "base_int")
    private Integer baseInt;

    @Column(name = "base_cha")
    private Integer baseCha;
}