package com.husnain.financetracker.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// This is the main security setup for the whole app.
// It decides which endpoints are open to everyone, which ones need a token and how the JWT filter fits in.
// @EnableWebSecurity turns on Spring Security's web security so the rules in here actually get used.
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor

public class SecurityConfig {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;

    // This builds the chain of security filters every request goes through before it reaches the controllers.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // CSRF protection is turned off because it's only needed when the browser sends cookies automatically
                // We use a token in the Authorization header instead which the browser never sends by itself so CSRF can't happen
                .csrf(csrf -> csrf.disable())
                // STATELESS means the server never keeps a session or remembers who's logged in.
                // Every request has to bring its own token which is the whole point of using JWTs.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // These paths are open to everyone without a token
                        // That's register and login, the Swagger docs and /error which Spring uses when something goes wrong
                        // If /error wasn't open, people would get a 401 instead of the real error.
                        .requestMatchers("/api/auth/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error")
                        .permitAll()
                        // Every other endpoint needs you to be logged in with a valid token.
                        .anyRequest().authenticated())
                // When someone isn't logged in and tries a locked endpoint, our RestAuthenticationEntryPoint sends back the 401 in our ApiError format.
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
                // Adds our JWT filter early in the chain so the user is already logged in by the time Spring checks the rules above.
                // It goes before UsernamePasswordAuthenticationFilter which is Spring's built in login filter.
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();

    }

    // This makes the AuthenticationManager available as a bean so AuthService can use it to check  the email and password at login.
    // Spring builds it for us using our CustomUserDetailsService to find the user and our BCrypt PasswordEncoder to check the password.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();

    }
}
