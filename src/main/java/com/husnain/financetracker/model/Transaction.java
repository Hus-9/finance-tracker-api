package com.husnain.financetracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

// This is the Transaction entity, each object is one row in the transactions table.
// There's no user field on purpose, a transaction belongs to a user through its account.
// So to check who owns a transaction we look at transaction -> account -> user.
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Transaction {

    //The primary key, the database counts it up by itself.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The account the money went in or out of, saved as account_id in the table.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // The amount of money, always positive, the type says if it's in or out.
    // BigDecimal stores decimals exactly, unlike double which would turn 0.1 + 0.2 into 0.30000000000000004.
    // precision 12 and scale 2 match NUMERIC(12,2) in the table.
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // INCOME or EXPENSE, it has to match the category's type.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    // An optional note like Tesco, it's allowed to be null.
    @Column(length = 255)
    private String description;

    // The day the money moved, LocalDate is just a date with no time, like 2026-10-14.
    @Column(nullable = false)
    private LocalDate date;

    // When the transaction was added to the app, it never changes after the first save.
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Runs right before the transaction gets saved for the first time so createdAt always get set.
    @PrePersist
    void onCreate() {

        createdAt = Instant.now();

    }
}
