package com.barlog.loyaltyapi.repository;

import com.barlog.loyaltyapi.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.lang.ScopedValue;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByIsActiveTrueOrderByIdDesc();
    List<Product> findByNameContainingIgnoreCase(String namePart);

    Optional<Product> findByName(String name);
}