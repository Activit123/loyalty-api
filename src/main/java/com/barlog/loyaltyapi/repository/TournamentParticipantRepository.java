package com.barlog.loyaltyapi.repository;
import com.barlog.loyaltyapi.model.TournamentParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TournamentParticipantRepository extends JpaRepository<TournamentParticipant, Long> {
    List<TournamentParticipant> findByTournamentId(Long tournamentId);
    boolean existsByTournamentIdAndUserId(Long tournamentId, Long userId);
    int countByTournamentId(Long tournamentId);
}