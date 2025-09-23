package com.prati.projetomercado.dto.request;

import java.math.BigDecimal;

public record ItemRequest(
        String name,
        String code,
        BigDecimal quantity,
        String unit,
        BigDecimal price
) {}
