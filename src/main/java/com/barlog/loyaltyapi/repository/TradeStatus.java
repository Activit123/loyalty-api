package com.barlog.loyaltyapi.repository;

public enum TradeStatus {
    INITIATED,      // Schimbul a fost creat.
    PENDING_APPROVAL, // Ambele părți și-au făcut oferta, dar așteaptă aprobarea finală.
    ACCEPTED,       // Ambele părți au acceptat oferta finală (gata de execuție).
    COMPLETED,      // Executat.
    CANCELED        // Anulat de oricare dintre părți.
}