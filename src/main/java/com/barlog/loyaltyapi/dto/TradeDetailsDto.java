package com.barlog.loyaltyapi.dto;

import com.barlog.loyaltyapi.repository.TradeStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

// DTO care combină Trade cu Ofertele celor doi utilizatori
@Data
public class TradeDetailsDto {
    private Long tradeId;
    private TradeStatus status;
    private LocalDateTime createdAt;
    
    // Detalii Initiator
    private UserResponseDto initiator;
    private List<TradeOfferItemDto> initiatorOffer;
    private boolean initiatorAgreed;
    
    // Detalii Recipient
    private UserResponseDto recipient;
    public List<TradeOfferItemDto> recipientOffer;
    private boolean recipientAgreed;
}