package com.prati.projetomercado.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record NfceRequest(
        SupermarketRequest supermarket,
        String accessKey,
        LocalDate date,
        BigDecimal totalPrice,
        List<Item> products
) {
    public record Item(
            String name,
            String code,
            BigDecimal quantity,
            String unit,
            BigDecimal price
    ) {
    }
}
