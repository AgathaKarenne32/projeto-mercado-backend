package com.prati.projetomercado.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RascunhoFilterRequest(
        String mercado,
        BigDecimal minTotal,
        BigDecimal maxTotal,
        LocalDate date
) {}
