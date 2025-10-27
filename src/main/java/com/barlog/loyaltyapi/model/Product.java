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
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "buy_price", nullable = false)
    private Integer buyPrice;

    @Column(name = "claim_value", nullable = false)
    private Integer claimValue;
    @Enumerated(EnumType.STRING)
    @Column
    private ProductCategory category;

//    @Column(nullable = true) // Stocul poate fi null pentru -1, dar vom gestiona asta
    private Integer stock;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}