package com.prati.projetomercado.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NfceFilterRequest(
        @Schema(description = "ID do supermercado", example = "1")
        Long supermarketId,

        @Schema(description = "Data da compra", example = "2025-10-01")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate date,

        @Schema(description = "Data da última atualização", example = "2025-11-01")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate updatedDate,

        @Schema(description = "Valor mínimo total da compra", example = "50.00")
        BigDecimal minTotal,

        @Schema(description = "Valor máximo total da compra", example = "200.00")
        BigDecimal maxTotal
) {}
