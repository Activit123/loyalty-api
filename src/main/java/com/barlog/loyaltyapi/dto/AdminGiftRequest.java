package com.barlog.loyaltyapi.dto;

import lombok.Data;

@Data
public class AdminGiftRequest {
    private String userEmail;
    private Long targetId; // ID-ul Itemului sau Produsului
    private String type;   // "ITEM" (rpg) sau "PRODUCT" (fizic)
}