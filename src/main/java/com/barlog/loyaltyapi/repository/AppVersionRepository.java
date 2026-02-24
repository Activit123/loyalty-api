// java/com/barlog/loyaltyapi/repository/AppVersionRepository.java
package com.barlog.loyaltyapi.repository;

import com.barlog.loyaltyapi.model.AppVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppVersionRepository extends JpaRepository<AppVersion, Long> {
    // Returnează ultima versiune adăugată pentru o platformă specifică
    Optional<AppVersion> findTopByPlatformOrderByCreatedAtDesc(String platform);
}