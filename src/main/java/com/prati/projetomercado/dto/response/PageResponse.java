package com.prati.projetomercado.dto.response;

import org.springframework.data.domain.Page;

public record PageResponse(
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static PageResponse from(Page<?> page) {
        return new PageResponse(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
