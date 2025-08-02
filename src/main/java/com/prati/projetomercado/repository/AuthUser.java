package com.prati.projetomercado.repository;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "authUsers")
@Entity(name = "authUser")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class AuthUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String email;
    
    private String password;
}
