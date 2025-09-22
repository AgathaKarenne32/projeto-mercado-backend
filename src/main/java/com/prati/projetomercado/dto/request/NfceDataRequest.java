package com.prati.projetomercado.dto.request;

import java.time.LocalDate;
import java.util.List;

public record NfceDataRequest(
        String store,
        String cnpj,
        AddressRequest address,
        String accessKey,
        LocalDate date,
        double totalPrice,
        List<ProductRequest> products
) {}
