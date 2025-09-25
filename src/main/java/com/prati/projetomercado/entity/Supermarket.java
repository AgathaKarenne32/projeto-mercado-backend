package com.prati.projetomercado.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@Table(
        name = "supermarket",
        uniqueConstraints = @UniqueConstraint(columnNames = {"cnpj", "manual", "created_by_user_id"})
)
@org.hibernate.annotations.Check(
        constraints = "(manual = TRUE AND created_by_user_id IS NOT NULL) OR " +
                "(manual = FALSE AND created_by_user_id IS NULL)"
)
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
    private AuthUser createdByUser;

    @OneToMany(mappedBy = "supermarket")
    private List<Purchase> purchases;

    @OneToMany(mappedBy = "supermarket")
    private List<Catalog> catalogItems;

    @CreationTimestamp
    private Instant creationDate;

    private boolean manual;
}
