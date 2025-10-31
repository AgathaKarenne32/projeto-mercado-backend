package com.prati.projetomercado.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

public record SuccessResponse<T>(
        String statusMessage,
        boolean success,
        T data,
        PageResponse page
) {
    public SuccessResponse(String statusMessage) {
        this(statusMessage, true, null, null);
    }

    public SuccessResponse(String statusMessage, T data) {
        this(statusMessage, true, data, null);
    }

    public SuccessResponse(String statusMessage, T data, PageResponse page) {
        this(statusMessage, true, data, page);
    }
}
