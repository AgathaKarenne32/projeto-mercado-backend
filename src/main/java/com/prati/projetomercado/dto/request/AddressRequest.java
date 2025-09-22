package com.prati.projetomercado.dto.request;

public record AddressRequest(
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state
) {}
