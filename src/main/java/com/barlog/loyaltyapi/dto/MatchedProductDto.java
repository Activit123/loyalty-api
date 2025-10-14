package com.barlog.loyaltyapi.dto;

import com.barlog.loyaltyapi.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchedProductDto {
    private ReceiptProductDto receiptItem; // Item-ul original de pe bon
    private Product shopProduct;          // Produsul corespunzător găsit în magazin (poate fi null)
}