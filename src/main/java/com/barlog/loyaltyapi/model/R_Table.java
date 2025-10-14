package com.barlog.loyaltyapi.model;
import jakarta.persistence.*;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor @Entity
@jakarta.persistence.Table(name = "tables")
public class R_Table {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private String name;
    @Column(nullable = false) private int capacity;
    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default private TableStatus status = TableStatus.AVAILABLE;
}