package com.prati.projetomercado.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

//atualizações de lombok
@Getter
@Setter
@NoArgsConstructor // cria um construtor sem argumentos (necessário para o JPA)
@AllArgsConstructor // cria um construtor com todos os argumentos
@Builder // padrão de projeto para construir objetos de forma fluida

// --- anotações do JPA/Hibernate ---
@Entity // marca esta classe como uma entidade que será mapeada para uma tabela no banco de dados
@Table(name = "rascunhos") // especifica o nome da tabela no banco de dados

// Adicionaremos as anotações do JPA/Hibernate
public class Rascunho {
    @Id // Marca este campo como a chave primária da tabela
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configura o ID para ser gerado automaticamente pelo banco
    private Long id;

    @Column(nullable = false) // Garante que o título não pode ser nulo no banco
    private String mercado;

    @Lob // Indica que este campo pode armazenar um grande volume de dados
    @Column(columnDefinition = "TEXT", nullable = false) // Define o tipo da coluna como TEXT e não permite nulo
    private String conteudo;

    @CreationTimestamp // Marca o campo para ser preenchido automaticamente com a data e hora de criação
    @Column(name = "created_at", updatable = false) // Nome da coluna e impede que seja atualizada
    private LocalDateTime createdAt;

    @UpdateTimestamp // Marca o campo para ser preenchido automaticamente com a data e hora da última atualização
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- Relacionamento com o Usuário ---
    @ManyToOne(fetch = FetchType.LAZY) // Define um relacionamento "Muitos-para-Um": Muitos rascunhos podem pertencer a Um usuário.
    @JoinColumn(name = "user_id", nullable = false) // Define a coluna de chave estrangeira (FK) na tabela 'rascunhos'
    private AuthUser user;
}
