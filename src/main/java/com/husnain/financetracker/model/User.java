package com.husnain.financetracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

// This is the User entity, each object of this class is one row in the users table.
// Same as Account, the table gets made by the Flyway migration and Hibernate just checks this class matches it.
// The Lombok annotations make the getters, setters, constructors and the builders for us.
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    // The primary key, the database makes it by counting up for every new user.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The users email which is also what they log in with.
    // Unique true means no two users can have the same email, the database has a unique rule on this column too.
    // AuthService makes it lowercase before saving so Alice@Example.com and alice@example.com count as the same email.
    @Column(nullable = false, unique = true)
    private String email;

    // The BCrypt hash of the password, never the real password.
    // It's called passwordHash instead of password so nobody gets confused and thinks the plain password is stored here.
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // When the user signed up, updatable false means it never changes after the first save.
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Runs right before the user gets saved for the first time so createdAt always gets set automatically.
    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}

