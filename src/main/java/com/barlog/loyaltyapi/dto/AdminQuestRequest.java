package com.barlog.loyaltyapi.dto;

import lombok.Data;

@Data
public class AdminQuestRequest {
    private String userEmail;
    private Long questId;
}