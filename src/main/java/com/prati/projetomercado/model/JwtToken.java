package com.prati.projetomercado.model;

import java.util.UUID;

public record JwtToken(
        String token, UUID refreshTokenId
) {
}
