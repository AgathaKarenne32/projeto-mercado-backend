package com.prati.projetomercado.dto.request;

public record PasswordRecoveryRequest(
        String email,
        String code,
        String newPassword
) {
}
