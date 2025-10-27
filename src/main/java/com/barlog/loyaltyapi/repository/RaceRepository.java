package com.barlog.loyaltyapi.repository;
import com.barlog.loyaltyapi.model.Race;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RaceRepository extends JpaRepository<Race, Long> {}