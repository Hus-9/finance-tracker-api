package com.husnain.financetracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// This class sets up how passwords get hashed before they go in the database so we never store anyone's real password.
// @Configuration tells Spring to load this class when the app starts.

@Configuration

public class PasswordConfig {

    // @Bean means Spring makes one PasswordEncoder and hands it to anything that needs it, like the register code and the login check.
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt adds a random salt to every password so two users with the same password still end up with different hashes.
        // It's also slow on purpose which makes brute forcing passwords way harder.
        // On login it hashes what the user typed and compares it to the saved hash, the hash never gets turned back into the real password.
        return new BCryptPasswordEncoder();

    }
}
