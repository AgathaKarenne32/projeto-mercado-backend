package com.prati.projetomercado.dto.request;

public record ProductRequest(
        String name,
        String code,
        Double quantity,
        String unit,
        double price
) {}
