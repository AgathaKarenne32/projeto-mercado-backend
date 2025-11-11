package com.prati.projetomercado.dto.request;

import java.math.BigDecimal;

public record CreateRascunhoRequest(
        String mercado,
        String conteudo,
        BigDecimal totalPrice
) {
}
