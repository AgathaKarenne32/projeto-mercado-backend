package com.prati.projetomercado.dto.request;

public record LoginUserRequest(
        String username,
        String email,
        String password,
        String confirmpassword
) {
}
