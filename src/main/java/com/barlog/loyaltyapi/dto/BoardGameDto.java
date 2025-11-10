package com.barlog.loyaltyapi.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BoardGameDto {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private String players;
    private String playTime;
    private String ageLimit;
    private String category;
    private Double complexityRating;
    private LocalDateTime createdAt;
}