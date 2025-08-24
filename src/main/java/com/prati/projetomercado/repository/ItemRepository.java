package com.prati.projetomercado.repository;

import com.prati.projetomercado.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
