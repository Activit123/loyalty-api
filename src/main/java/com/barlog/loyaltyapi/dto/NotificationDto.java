package com.barlog.loyaltyapi.dto;

import com.barlog.loyaltyapi.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private String message;
    private NotificationType type;
    private String link;
    private boolean isRead;
    private LocalDateTime createdAt;
}