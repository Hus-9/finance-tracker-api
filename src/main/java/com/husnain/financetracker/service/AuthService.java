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
import com.husnain.financetracker.model.Category;
import com.husnain.financetracker.model.TransactionType;
import com.husnain.financetracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// This is where the logic for signing up and logging in happens, AuthController passes everything in here.
@Service
@RequiredArgsConstructor

public class AuthService {

    // A small record to hold the name and type of one default category.
    record DefaultCategory(String name, TransactionType type){

    }

    //Every new user gets these categories when they sign up so they can start adding transactions straight away.
    // static final means there's only one copy of this list and it never changes.
    private static final List<DefaultCategory> DEFAULT_CATEGORIES = List.of(
            new DefaultCategory("Salary", TransactionType.INCOME),
            new DefaultCategory("Rent", TransactionType.EXPENSE),
            new DefaultCategory("Food", TransactionType.EXPENSE),
            new DefaultCategory("Transport", TransactionType.EXPENSE),
            new DefaultCategory("Bills", TransactionType.EXPENSE),
            new DefaultCategory("Entertainment", TransactionType.EXPENSE));

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CategoryRepository categoryRepository;

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

        // Turns each default into a real Category for this user and saves them all in one go.
        // It's inside the same @Transactional as saving the user so if this fails the user doesn't get saved either.
        List<Category> categories = DEFAULT_CATEGORIES.stream()
                .map(def -> Category.builder()
                        .user(saved)
                        .name(def.name())
                        .type(def.type())
                        .build())
                .toList();
        categoryRepository.saveAll(categories);

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
