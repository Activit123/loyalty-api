package com.barlog.loyaltyapi.repository;

import com.barlog.loyaltyapi.model.R_Table;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import java.util.List;
import java.util.Optional;

public interface TableRepository extends JpaRepository<R_Table, Long> {
    List<R_Table> findAllByOrderByIdAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<R_Table> findWithLockingById(Long id);
}