package com.prati.projetomercado.dto.response;

import java.util.UUID;

public record AuthResponse(
        String accessToken, UUID refreshToken, UserResponse user

) {
}
