package com.prati.projetomercado.repository;

import com.prati.projetomercado.entity.AuthUser;
import com.prati.projetomercado.entity.Supermarket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupermarketRepository extends JpaRepository<Supermarket, Long> {
    Optional<Supermarket> findByCnpjAndCreatedByUser(String cnpj, AuthUser createdByUser);

    List<Supermarket> findAllByCreatedByUser(AuthUser createdByUser);

}
