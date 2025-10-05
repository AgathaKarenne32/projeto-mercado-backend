package com.prati.projetomercado.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AuthUser user;

    @ManyToOne
    @JoinColumn(name = "supermarket_id")
    private Supermarket supermarket;

    @Column(name = "access_key", unique = true)
    private String accessKey;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Item> items = new ArrayList<>();

    @CreationTimestamp
    private Instant creationDate;

    @Column(name = "manual", nullable = false)
    private boolean manual;
}
