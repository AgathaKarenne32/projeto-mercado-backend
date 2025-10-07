package com.prati.projetomercado.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
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
    private String city;
    private String state;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private AuthUser createdByUser;

    @OneToMany(mappedBy = "supermarket")
    private List<Purchase> purchases;

    @OneToMany(mappedBy = "supermarket")
    private List<Catalog> catalogItems;

    @CreationTimestamp
    private Instant creationDate;

    @Column(name = "manual", nullable = false)
    private boolean manual;
}
