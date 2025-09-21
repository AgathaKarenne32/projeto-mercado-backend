package com.prati.projetomercado.repository;

import com.prati.projetomercado.entity.AuthUser;
import com.prati.projetomercado.entity.Rascunho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    List<Rascunho> findByUser(AuthUser user);

}