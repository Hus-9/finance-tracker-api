package com.husnain.financetracker.repository;

import com.husnain.financetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// This is how we talk to the users table in the database.
// Same as the other repositories, Spring Data makes the real class and builds the queries from the method names.
// JpaRepository<User, Long> gives us the basic stuff like save and findById for free.
public interface UserRepository extends JpaRepository<User, Long> {

    // Checks if a user with this email already exists, it just gives back true or false.
    // AuthService uses it when someone registers so we can send a 409 if the email is already taken.
    // It's quicker than loading the whole user when we only need to know if they're there
    boolean existsByEmail(String email);

    // Gets the user with this email, CustomUserDetailsService uses it to find the user when they log in.
    // It returns an Optional because there might not be a user with that email
    Optional<User> findByEmail(String email);

}
