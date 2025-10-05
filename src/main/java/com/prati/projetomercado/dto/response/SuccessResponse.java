package com.prati.projetomercado.dto.response;

public record SuccessResponse<T>(
        String statusMessage,
        boolean success,
        T data
) {
    public SuccessResponse(String statusMessage) {
        this(statusMessage, true, null);
    }

    public SuccessResponse(String statusMessage, T data) {
        this(statusMessage, true, data);
    }
}
