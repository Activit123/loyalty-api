package com.barlog.loyaltyapi.repository;

import com.barlog.loyaltyapi.model.BoardGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BoardGameRepository extends JpaRepository<BoardGame, Long> {
    /**
     * Găsește toate jocurile, sortate după categorie ascendent și nume ascendent.
     */
    List<BoardGame> findAllByOrderByCategoryAscNameAsc();
}