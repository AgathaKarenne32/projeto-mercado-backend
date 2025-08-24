package com.prati.projetomercado.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "catalog",
        uniqueConstraints = @UniqueConstraint(columnNames = {"supermarket_id", "code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Catalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private String name;
    private String unit;

    @ManyToOne
    @JoinColumn(name = "supermarket_id")
    private Supermarket supermarket;

    @OneToMany(mappedBy = "catalog")
    private List<Item> items;
}
