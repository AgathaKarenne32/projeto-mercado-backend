package com.prati.projetomercado.dto.response;

import com.prati.projetomercado.dto.request.NfceRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record NfceResponse(
        SupermarketResponse supermarket,
        String accessKey,
        LocalDate date,
        BigDecimal totalPrice,
        Boolean isManual,
        List<NfceRequest.Item> products
) {
}
