package com.prati.projetomercado.dto.request;

public record SupermarketRequest(
        Long id,
        String store,
        String cnpj,
        String city,
        String state
) {
}
