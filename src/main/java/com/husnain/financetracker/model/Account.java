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

import java.time.Instant;

// This is the Account entity, each object of this class is one row in the accounts table.
// The table itself gets made by the Flyway migration, Hibernate just checks this class matches it when the app starts.
// @Entity tells JPA this class maps to a table and @Table says which table.
// @Getter and @Setter are Lombok making all the getters and setters so we don't have to write them.
// @NoArgsConstructor is needed because JPA has to be able to make an empty object when it loads a row.
// @Builder lets us make accounts like Account.builder().name(...).build() and @AllArgsConstructor is there because Builder needs it.
@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Account {

    // The primary key, IDENTITY means the database makes the id itself by counting up for every new row.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user this account belongs to, one user can have lots of accounts.
    // In the table this is saved as user_id which points at the users table.
    // LAZY means the user doesn't get loaded from the database until we actually use it which saves a query when we don't need it.
    // optional false and nullable false means every account has to belong to a user
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // The account name, the 100 limit matches the @Size max in AccountRequest.
    @Column(nullable = false, length = 100)
    private String name;

    // The account type, EnumType.STRING saves the enum name as text instead of its position number.
    // That way if we add or move values in AccountType later, the old rows still mean the same things.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType type;

    // The 3 letter currency code like GBP which is why the length is 3.
    @Column(nullable = false, length = 3)
    private String currency;

    // When the account was made, updatable false means it never gets changed after the first save.
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // @PrePersist runs right before the account gets saved for the first time so createdAt always gets set automatically.
    // Even if someone sets createdAt with the builder, this overwrites it with the real time
    @PrePersist
    void onCreate() {

        createdAt = Instant.now();

    }
}
