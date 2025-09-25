package com.prati.projetomercado.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record NfceDataRequest(
        String store,
        String cnpj,
        Address address,
        String accessKey,
        LocalDate date,
        BigDecimal totalPrice,
        List<Item> products
) {
    public record Address(
            String street,
            String number,
            String complement,
            String neighborhood,
            String city,
            String state
    ) {}

    public record Item(
            String name,
            String code,
            BigDecimal quantity,
            String unit,
            BigDecimal price
    ) {}
}