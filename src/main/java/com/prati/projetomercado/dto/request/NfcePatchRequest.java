package com.prati.projetomercado.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record NfcePatchRequest(
        LocalDate date,
        BigDecimal totalPrice,
        List<NfceRequest.Item> products
) {
}
