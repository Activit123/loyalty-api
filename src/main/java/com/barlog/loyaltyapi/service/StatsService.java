// src/main/java/com/barlog/loyaltyapi/service/StatsService.java

package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.DashboardStatsDTO;
import com.barlog.loyaltyapi.repository.StatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    private final StatsRepository statsRepository;

    @Autowired
    public StatsService(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    public DashboardStatsDTO getDashboardStats() {
        long userCount = statsRepository.countActiveUsers();
        long productCount = statsRepository.countAvailableProducts();
        long coinsSum = statsRepository.sumTotalAwardedCoins(); // Apelăm metoda corectă
        long announcementCount = statsRepository.countActiveAnnouncements();

        return new DashboardStatsDTO(userCount, productCount, coinsSum, announcementCount);
    }
}