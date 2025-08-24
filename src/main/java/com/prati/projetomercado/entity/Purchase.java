package com.prati.projetomercado.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "purchase")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "supermarket_id")
    private Supermarket supermarket;

    @Column(name = "access_key", unique = true)
    private String accessKey;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "total_price")
    private Double totalPrice;

    @OneToMany(mappedBy = "purchase")
    private List<Item> items;
}
