package com.prati.projetomercado.dto.request;

public record CreateUserRequest(
        String username,
        String email,
        String password,
        String confirmPassword
) {}
