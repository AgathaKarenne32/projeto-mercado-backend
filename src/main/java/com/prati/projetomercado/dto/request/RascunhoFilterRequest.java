package com.prati.projetomercado.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RascunhoFilterRequest(

        @Schema(description = "Nome do mercado", example = "Carrefour")
        String mercado,

        @Schema(description = "Valor mínimo total do rascunho", example = "50.00")
        BigDecimal minTotal,

        @Schema(description = "Valor máximo total do rascunho", example = "200.00")
        BigDecimal maxTotal,

        @Schema(description = "Data de criação do rascunho", example = "2025-11-11")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate date
) {}
