package com.prati.projetomercado.repository;

import com.prati.projetomercado.entity.AuthUser;
import com.prati.projetomercado.entity.Supermarket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupermarketRepository extends JpaRepository<Supermarket, Long> {
    Optional<Supermarket> findByCnpjAndManual(String cnpj, Boolean isManual);

    Optional<Supermarket> findByIdAndCreatedByUserAndManual(Long id, AuthUser createdByUser, Boolean isManual);

    Page<Supermarket> findAllByCreatedByUser(AuthUser user, Pageable pageable);
}
