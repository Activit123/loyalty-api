package com.barlog.loyaltyapi.repository;

import com.barlog.loyaltyapi.model.ItemTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemTemplateRepository extends JpaRepository<ItemTemplate, Long> {
    // Returnează doar itemele active pentru magazin
    List<ItemTemplate> findByIsActiveTrue();
}