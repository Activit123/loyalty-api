package com.barlog.loyaltyapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ReceiptProductDto {
    private String descriere;
    private Double cantitate; // Folosim Double pentru a permite cantități zecimale (ex: 1.5 kg)
    @JsonProperty("pret_total") // Mapăm numele din JSON la numele câmpului Java
    private Double pretTotal;
}