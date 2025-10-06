package com.prati.projetomercado.dto.response;

public record UserResponse(
        String username,
        String email
) {
    public String toJson() {
        return "{username='"+username+"',email='"+email+"'}";
    }
}
