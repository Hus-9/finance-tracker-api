package com.husnain.financetracker.controller;

import com.husnain.financetracker.dto.AuthResponse;
import com.husnain.financetracker.dto.LoginRequest;
import com.husnain.financetracker.dto.RegisterRequest;
import com.husnain.financetracker.dto.UserResponse;
import com.husnain.financetracker.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

//This controller handles signing up and logging in.
//These are the endpoints people hit before they have a token because you need them to get a token in the first place.
//Same as the other controllers, it doesn't do the actual logic, it just passes everything to AuthService.
//@RequestMapping means start with /api/auth.

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@SecurityRequirements

public class AuthController {

    private final AuthService authService;

    // POST /api/auth/register makes a new user.
    // @Valid checks the RegisterRequest rules first so stuff like a missing email or a short password gets rejected straight away.
    // It returns 201 Created with the new user details but no token so they still have to log in after.
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {

        return authService.register(request);

    }

    // POST /api/auth/login checks the email and password.
    // If they're right, it sends back an AuthResponse with the JWT, which the user then puts in the Authorization header for every other request.
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {

        return authService.login(request);

    }
}

