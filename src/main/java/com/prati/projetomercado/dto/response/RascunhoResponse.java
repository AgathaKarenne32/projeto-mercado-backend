package com.prati.projetomercado.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record RascunhoResponse(
        Long id,
        String mercado,
        String conteudo,
        BigDecimal totalPrice,
        Instant createdAt,
        Instant updatedAt
) {
}
