package com.prati.projetomercado.dto.response;

import java.time.LocalDateTime;

public record RascunhoResponse(
        Long id,
        String titulo,
        String conteudo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long userId,
        String username
) {
}