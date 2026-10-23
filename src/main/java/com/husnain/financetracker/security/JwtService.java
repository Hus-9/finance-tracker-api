package com.husnain.financetracker.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

// This class handles everything to do with JWTs, making them when someone logs in and reading them on every request.
// The tokens are signed with our secret key so nobody can change what's inside them without us knowing.
// They're not encrypted though, anyone with the token can read the email in it, they just can't fake or edit it.
@Service

public class JwtService {

    private final SecretKey key;
    private final long expirationMinutes;

    // @Value pulls the secret and the expiry time from application.yml so they're not hardcoded in the code.
    // The secret is stored in base64 so it gets decoded first, then turned into the key we sign tokens with.
    // The decoded secret has to be at least 32 bytes or hmacShaKeyFor throws an error and the app won't start.
    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMinutes = expirationMinutes;

    }

    // Makes a new token for a user after they log in.
    public String generateToken(UserDetails user) {

        long now = System.currentTimeMillis();
        return Jwts.builder()
                //The subject is who the token is for, getUsername gives the email because that's what we log in with.
                .subject(user.getUsername())
                // When the token was made.
                .issuedAt(new Date(now))
                // When the token stops working, the minutes get turned into milliseconds because Date works in milliseconds.
                .expiration(new Date(now + expirationMinutes * 60 * 1000))
                // Signs it with our secret key, this is what makes it impossible to fake.
                .signWith(key)
                // Turns it all into the actual token string that gets sent to the client
                .compact();
    }

    // Reads the email out of a token, the JwtAuthenticationFilter calls this on every request.
    // It gives back an Optional so it's empty if the token is no good.
    public Optional<String> extractEmail(String token) {
        try {

            // verifyWith checks the signature matches our key and parseSignedClaims also checks it hasn't expired..
            // If both are fine it gets the payload and takes the subject out of it which is the email.
            String email = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Optional.ofNullable(email);

        } catch (JwtException | IllegalArgumentException e) {

            // JwtException covers fake, expired or broken tokens and IllegalArgumentException is for an empty token.
            // Either way we don't crash, we just say there's no valid email so the request isn't logged in.
            return Optional.empty();

        }
    }

    // How long a token lasts in seconds, this is what goes in expiresInSeconds in the AuthResponse.
    public long getExpirationSeconds() {

        return expirationMinutes * 60;

    }
}
