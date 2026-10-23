package com.husnain.financetracker.service;

import com.husnain.financetracker.dto.AuthResponse;
import com.husnain.financetracker.dto.LoginRequest;
import com.husnain.financetracker.dto.RegisterRequest;
import com.husnain.financetracker.dto.UserResponse;
import com.husnain.financetracker.exception.ConflictException;
import com.husnain.financetracker.model.User;
import com.husnain.financetracker.repository.UserRepository;
import com.husnain.financetracker.security.JwtService;
import com.husnain.financetracker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// This is where the logic for signing up and logging in happens, AuthController passes everything in here.
@Service
@RequiredArgsConstructor

public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    // Makes a new user and saves them in the database.
    @Transactional
    public UserResponse register(RegisterRequest request) {

        // Makes the email lowercase so Alice@Example.com and alice@example.com count as the same email.
        String email = request.email().toLowerCase();

        // If someone already has this email it stops here and sends back a 409.
        // The unique rule on the email column in the database is a backup in case two people sign up with the same email at the exact same time.
        if (userRepository.existsByEmail(email)) {

            throw new ConflictException("An account with this email already exists");

        }

        // Builds the new user, the password gets hashed with BCrypt first so the real password never gets saved.
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        // Saves the user and sends back their details but no token so they still have to log in after.
        User saved = userRepository.save(user);
        return UserResponse.from(saved);

    }

    // Checks the email and password and gives back a JWT if they're right.
    public AuthResponse login(LoginRequest request) {

        // The AuthenticationManager does the actual checking, it finds the user with CustomUserDetailsService and checks the password with BCrypt.
        // The email gets made lowercase again so it matches how it was saved at register.
        // If the email or password is wrong, it throws an exception and GlobalExceptionHandler turns it into a 401.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password()));

        // If we get here the login worked, getPrincipal gives back the UserPrincipal that CustomUserDetailsService made.
        // The cast is safe because our UserDetailsService always gives back a UserPrincipal.
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        // Makes the token and sends it back with the type and how many seconds it lasts.
        String token =  jwtService.generateToken(user);
        return new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds());

    }
}
