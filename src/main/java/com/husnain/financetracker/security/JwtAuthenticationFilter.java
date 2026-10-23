package com.husnain.financetracker.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// This filter runs on every request before it gets to the controllers and checks if there's a valid JWT.
// If the token is good it logs the user in for that one request so @AuthenticationPrincipal works in the controllers.
// If there's no token, or it's bad it doesn't block anything itself, it just leaves the request as not logged in
// Spring Security then blocks the request later if the endpoint needs you to be logged in.
// OncePerRequestFilter makes sure this only runs once per request even if the request gets passed around inside Spring.
// It's not a @Component because SecurityConfig makes it with new and adds it to the filter chain itself.
@RequiredArgsConstructor

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        // Gets the Authorization header from the request, it should look like Bearer then the token.
        String header = request.getHeader("Authorization");

        // Only bother checking if there's a header and it actually starts with Bearer.
        if (header != null && header.startsWith("Bearer ")) {

            // Cuts off the first 7 characters which is Bearer and the space so we're left with just the token.
            String token = header.substring(7);
            // JwtService checks the token's signature and expiry and gives back the email inside it.
            // If the token is fake, expired or broken it comes back empty and nothing in here runs.
            jwtService.extractEmail(token).ifPresent(email -> {

                try {

                    // Loads the user fresh from the database every request so a deleted user can't keep using an old token.
                    UserDetails user = userDetailsService.loadUserByUsername(email);
                    // Makes the logged in user object for Spring Security.
                    // The password is null because they've already proved who they are with the token.
                    // getAuthorities is the user's roles and permissions.
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    // Saves the user into the security context which is what marks this request as logged in.
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                } catch (UsernameNotFoundException e) {

                    // The token is valid but the user was deleted: treat the request as not logged in.

                }
            });
        }

        // Passes the request on to the next filter so it carries on to the controller whether they're logged in or not
        filterChain.doFilter(request, response);

    }
}
