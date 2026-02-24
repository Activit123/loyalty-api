package com.barlog.loyaltyapi.repository;
import com.barlog.loyaltyapi.model.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    List<Tournament> findAllByOrderByStartTimeDesc();
}