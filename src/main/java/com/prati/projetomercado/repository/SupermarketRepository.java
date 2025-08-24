package com.prati.projetomercado.repository;

import com.prati.projetomercado.entity.Supermarket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupermarketRepository extends JpaRepository<Supermarket, Long> {
    Optional<Supermarket> findByCnpj(String cnpj);
}
