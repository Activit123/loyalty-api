package com.barlog.loyaltyapi.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TournamentMatchDto {
    private Long id;
    private Long tournamentId;
    private Integer roundNumber;
    private Integer matchOrder;
    private Long nextMatchId;
    
    // Folosim UserResponseDto sau un obiect simplificat pentru a trimite detaliile jucătorilor
    private UserResponseDto player1;
    private UserResponseDto player2;
    private UserResponseDto winner;
}