package com.prati.projetomercado.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter; // adicionado
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "authUsers")
@Entity(name = "authUser")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AuthUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String email;

    private String username;
    
    private String password;

    @CreationTimestamp
    private Instant creationDate;

    // @Builder.Default garante que, ao criar um usuário, este campo comece como 'false'.
    @Builder.Default
    private boolean enabled = false;

    // Campo para guardar o token de confirmação de e-mail que será enviado ao usuário.
    @Column(name = "confirmation_token")
    private String confirmationToken;

    // Campo para guardar a data de expiração do token de confirmação.
    @Column(name = "confirmation_token_expiry")
    private LocalDateTime confirmationTokenExpiry;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )


    private List<AccountRole> roles = new ArrayList<>();


    @OneToMany(mappedBy = "authUser", cascade = CascadeType.ALL)
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @OneToMany(mappedBy = "authUser", cascade = CascadeType.ALL)
    private List<AccessToken> accessTokens = new ArrayList<>();

    @OneToMany(mappedBy = "createdByUser")
    private List<Supermarket> createdSupermarkets;

    @OneToMany(mappedBy = "user")
    private List<Purchase> purchases;
}
