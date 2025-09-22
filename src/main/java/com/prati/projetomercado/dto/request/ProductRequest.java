package com.prati.projetomercado.dto.request;

import java.math.BigDecimal;

public record ProductRequest(
        String name,
        String code,
        Double quantity,
        String unit,
        BigDecimal price
) {}
