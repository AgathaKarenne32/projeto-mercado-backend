package com.prati.projetomercado.repository;

import com.prati.projetomercado.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    Optional<Purchase> findByAccessKey(String accessKey);
}
