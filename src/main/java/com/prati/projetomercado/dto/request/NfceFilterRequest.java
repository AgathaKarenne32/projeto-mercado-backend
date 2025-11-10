package com.prati.projetomercado.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NfceFilterRequest(
        Long supermarketId,
        LocalDate date,
        LocalDate updatedDate,
        BigDecimal minTotal,
        BigDecimal maxTotal
) {}
