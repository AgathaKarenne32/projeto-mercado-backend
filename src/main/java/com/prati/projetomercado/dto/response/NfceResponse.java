package com.prati.projetomercado.dto.response;

import com.prati.projetomercado.dto.request.NfceRequest;
import com.prati.projetomercado.entity.Purchase;
import com.prati.projetomercado.entity.Supermarket;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record NfceResponse(
        SupermarketResponse supermarket,
        String accessKey,
        LocalDate date,
        BigDecimal totalPrice,
        Boolean isManual,
        Instant createdAt,
        Instant updatedAt,
        List<NfceRequest.Item> products
) {
    public static NfceResponse from(Purchase purchase) {
        Supermarket market = purchase.getSupermarket();
        SupermarketResponse supermarket = SupermarketResponse.from(market);

        List<NfceRequest.Item> items = purchase.getItems().stream()
                .map(item -> new NfceRequest.Item(
                        item.getCatalog().getName(),
                        item.getCatalog().getCode(),
                        item.getQuantity(),
                        item.getCatalog().getUnit(),
                        item.getUnitPrice()
                ))
                .toList();

        return new NfceResponse(
                supermarket,
                purchase.getAccessKey(),
                purchase.getDate(),
                purchase.getTotalPrice(),
                purchase.isManual(),
                purchase.getCreatedAt(),
                purchase.getUpdatedAt(),
                items
        );
    }

    public record Item(
            String name,
            String code,
            BigDecimal quantity,
            String unit,
            BigDecimal price
    ) {
    }
}
