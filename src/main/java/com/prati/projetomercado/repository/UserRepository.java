package com.prati.projetomercado.repository;

import com.prati.projetomercado.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
