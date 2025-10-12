package com.prati.projetomercado.dto.request;

public record ChangePasswordRequest(
    String currentPassword,
    String newPassword,
    String confirmNewPassword
)   {
}
