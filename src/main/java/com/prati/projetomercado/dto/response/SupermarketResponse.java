package com.prati.projetomercado.dto.response;

import com.prati.projetomercado.entity.Supermarket;

public record SupermarketResponse(
        Long id,
        String store,
        String cnpj,
        String city,
        String state,
        Boolean isManual
) {
    public static SupermarketResponse from(Supermarket market) {
        return new SupermarketResponse(
                market.getId(),
                market.getName(),
                market.getCnpj(),
                market.getCity(),
                market.getState(),
                market.isManual()
        );
    }
}
