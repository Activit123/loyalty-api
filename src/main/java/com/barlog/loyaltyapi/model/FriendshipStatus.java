package com.barlog.loyaltyapi.model;

public enum FriendshipStatus {
    PENDING,    // Cerere trimisă, în așteptarea acceptării
    ACCEPTED,   // Prietenie confirmată (vizibil în listă)
    REJECTED,   // Cerere respinsă
    BLOCKED     // Un utilizator l-a blocat pe celălalt (poate fi util în viitor)
}