// src/main/java/com/barlog/loyaltyapi/dto/DashboardStatsDTO.java

package com.barlog.loyaltyapi.dto;

public class DashboardStatsDTO {

    private long activeUsers;
    private long availableProducts;
    private long totalCoinsAwarded; // Revenim la numele original, acum este corect
    private long activeAnnouncements;

    public DashboardStatsDTO(long activeUsers, long availableProducts, long totalCoinsAwarded, long activeAnnouncements) {
        this.activeUsers = activeUsers;
        this.availableProducts = availableProducts;
        this.totalCoinsAwarded = totalCoinsAwarded;
        this.activeAnnouncements = activeAnnouncements;
    }

    // Getters
    public long getActiveUsers() { return activeUsers; }
    public long getAvailableProducts() { return availableProducts; }
    public long getTotalCoinsAwarded() { return totalCoinsAwarded; }
    public long getActiveAnnouncements() { return activeAnnouncements; }
}