package com.prati.projetomercado.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accessToken")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "auth_user_id_fk", referencedColumnName = "id")
    private AuthUser authUser;

    private String token;

    @CreationTimestamp
    private Instant creationDate;

    private Instant expiredDate;
}
