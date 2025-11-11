package com.prati.projetomercado.repository.spec;

import com.prati.projetomercado.entity.AuthUser;
import com.prati.projetomercado.entity.Rascunho;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public class RascunhoSpecification {

    public static Specification<Rascunho> belongsToUser(AuthUser user) {
        return (root, query, cb) ->
                user == null ? null : cb.equal(root.get("user"), user);
    }

    public static Specification<Rascunho> hasMercado(String mercado) {
        return (root, query, cb) ->
                mercado == null || mercado.isEmpty()
                        ? null
                        : cb.like(cb.lower(root.get("mercado")), "%" + mercado.toLowerCase() + "%");
    }

    public static Specification<Rascunho> hasTotalBetween(BigDecimal minTotal, BigDecimal maxTotal) {
        return (root, query, cb) -> {
            if (minTotal == null && maxTotal == null) return null;
            if (minTotal != null && maxTotal != null)
                return cb.between(root.get("totalPrice"), minTotal, maxTotal);
            if (minTotal != null)
                return cb.greaterThanOrEqualTo(root.get("totalPrice"), minTotal);
            return cb.lessThanOrEqualTo(root.get("totalPrice"), maxTotal);
        };
    }

    public static Specification<Rascunho> hasCreatedDate(LocalDate date) {
        return (root, query, cb) -> {
            if (date == null) return null;

            Instant startOfDay = date.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant endOfDay = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

            return cb.between(root.get("createdAt"), startOfDay, endOfDay);
        };
    }
}
