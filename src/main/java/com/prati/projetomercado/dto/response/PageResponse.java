package com.prati.projetomercado.dto.response;

import org.springframework.data.domain.Page;

public record PageResponse(
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean last
) {
    public PageResponse(Page<NfceResponse> page) {
        this(page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }
}
