package com.barlog.loyaltyapi.repository;
import com.barlog.loyaltyapi.model.TournamentMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TournamentMatchRepository extends JpaRepository<TournamentMatch, Long> {
    List<TournamentMatch> findByTournamentIdOrderByRoundNumberAscMatchOrderAsc(Long tournamentId);
}