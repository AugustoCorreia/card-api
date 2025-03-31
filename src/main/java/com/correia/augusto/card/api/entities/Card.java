package com.correia.augusto.card.api.entities;

import com.correia.augusto.card.api.enums.CardType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "card_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String number;

    @Column(nullable = false)
    private String holderName;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false)
    private String cvv;

    @Column(nullable = false)
    private String lote;

    @Column(name = "data_processamento")
    private LocalDate dataProcessamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardType type;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}