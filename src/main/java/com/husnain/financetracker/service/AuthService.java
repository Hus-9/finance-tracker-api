package com.husnain.financetracker.service;

import com.husnain.financetracker.dto.AuthResponse;
import com.husnain.financetracker.dto.LoginRequest;
import com.husnain.financetracker.dto.RegisterRequest;
import com.husnain.financetracker.dto.UserResponse;
import com.husnain.financetracker.model.User;
import com.husnain.financetracker.repository.UserRepository;
import com.husnain.financetracker.security.JwtService;
import com.husnain.financetracker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
@RequiredArgsConstructor

public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public UserResponse register(RegisterRequest request) {

        String email = request.email().toLowerCase();

        if (userRepository.existsByEmail(email)) {

            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exist");

        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        User saved = userRepository.save(user);
        return UserResponse.from(saved);

    }

    public AuthResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password()));

        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        String token =  jwtService.generateToken(user);
        return new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds());

    }
}
