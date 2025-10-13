package com.barlog.loyaltyapi.repository;

import com.barlog.loyaltyapi.model.Role;
import com.barlog.loyaltyapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByRole(Role role);
    List<User> findTop10ByOrderByCoinsDesc();

}