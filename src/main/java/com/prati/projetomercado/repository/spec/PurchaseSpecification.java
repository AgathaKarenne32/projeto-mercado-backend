package com.prati.projetomercado.repository.spec;

import com.prati.projetomercado.entity.Purchase;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public class PurchaseSpecification {

    public static Specification<Purchase> hasSupermarket(Long supermarketId) {
        return (root, query, cb) ->
                supermarketId == null ? null :
                        cb.equal(root.get("supermarket").get("id"), supermarketId);
    }

    public static Specification<Purchase> hasDate(LocalDate date) {
        return (root, query, cb) ->
                date == null ? null : cb.equal(root.get("date"), date);
    }

    public static Specification<Purchase> hasUpdatedDate(LocalDate updatedDate) {
        return (root, query, cb) -> {
            if (updatedDate == null) return null;

            Instant startOfDay = updatedDate.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant endOfDay = updatedDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

            return cb.between(root.get("updatedDate"), startOfDay, endOfDay);
        };
    }

    public static Specification<Purchase> hasTotalBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min == null) return cb.lessThanOrEqualTo(root.get("totalPrice"), max);
            if (max == null) return cb.greaterThanOrEqualTo(root.get("totalPrice"), min);
            return cb.between(root.get("totalPrice"), min, max);
        };
    }
}
