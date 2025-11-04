package com.prati.projetomercado.repository;

import com.prati.projetomercado.entity.AuthUser;
import com.prati.projetomercado.entity.Rascunho;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository // Anotação que marca esta interface como um componente de repositório do Spring
public interface RascunhoRepository extends JpaRepository<Rascunho, Long> {

    // --- MÉTODOS MÁGICOS DO SPRING DATA JPA ---

    /**
     * O Spring Data JPA é inteligente o suficiente para criar automaticamente
     * uma consulta que busca todos os Rascunhos que pertencem a um usuário específico.
     * Só precisamos declarar o método com este nome.
     *
     * @param user O usuário cujos rascunhos queremos encontrar.
     * @return Uma lista de rascunhos pertencentes ao usuário.
     */
    Page<Rascunho> findByUser(AuthUser user, Pageable pageable);

    Optional<Rascunho> findRascunhoByUserAndId(AuthUser user, Long id);

    void deleteRascunhoByUserAndId(AuthUser user, Long id);

    List<Rascunho> getRascunhoByUserAndId(AuthUser user, Long id);
}
