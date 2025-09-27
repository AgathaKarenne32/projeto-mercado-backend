package com.prati.projetomercado.dto.response;

import java.time.LocalDateTime;

public record RascunhoResponse(
        Long id,
        String mercado,
        String conteudo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}