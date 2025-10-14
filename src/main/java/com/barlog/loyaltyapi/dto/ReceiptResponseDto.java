package com.barlog.loyaltyapi.dto;

import lombok.Data;
import java.util.List;

@Data
public class ReceiptResponseDto {
    private String cui;
    private String data;
    private String ora;
    private List<ReceiptProductDto> produse;
}