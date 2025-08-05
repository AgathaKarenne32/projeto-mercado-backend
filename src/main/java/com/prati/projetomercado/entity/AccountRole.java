package com.prati.projetomercado.entity;

import com.prati.projetomercado.model.AccountRoleEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "account_roles")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AccountRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AccountRoleEnum roleName;
}
