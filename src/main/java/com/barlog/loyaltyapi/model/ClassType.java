package com.barlog.loyaltyapi.model;
import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "class_types")
public class ClassType {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String name;
    @Column(columnDefinition = "TEXT") private String description;
    private String requiredAttribute;
    private String gameTypeBonusCategory;
    private Double gameTypeXpMultiplier;
    private String discountCategory;
    private Double discountPercentage;
}