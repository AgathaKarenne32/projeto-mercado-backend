package com.prati.projetomercado.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record NfceDataRequest(
        String store,
        String cnpj,
        AddressRequest address,
        String accessKey,
        LocalDate date,
        BigDecimal totalPrice,
        List<ProductRequest> products
) {}
