package com.prati.projetomercado.repository;

import com.prati.projetomercado.entity.AuthUser;
import com.prati.projetomercado.entity.Purchase;
import com.prati.projetomercado.entity.Supermarket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    Optional<Purchase> findByAccessKey(String accessKey);

    Optional<Purchase> findBySupermarket(Supermarket supermarket);

    Page<Purchase> findAllByUser(AuthUser user, Pageable pageable);
}
