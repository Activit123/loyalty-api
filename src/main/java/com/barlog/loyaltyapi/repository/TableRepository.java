package com.barlog.loyaltyapi.repository;
import com.barlog.loyaltyapi.model.R_Table;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface TableRepository extends JpaRepository<R_Table, Long> {
    List<R_Table> findAllByOrderByIdAsc();
}