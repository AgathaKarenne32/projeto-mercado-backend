package com.prati.projetomercado.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "supermarket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supermarket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String cnpj;
    private String street;
    private String number;
    private String complement;
    private String neighborhood;
    private String city;
    private String state;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    @OneToMany(mappedBy = "supermarket")
    private List<Purchase> purchases;

    @OneToMany(mappedBy = "supermarket")
    private List<Catalog> catalogItems;
}
