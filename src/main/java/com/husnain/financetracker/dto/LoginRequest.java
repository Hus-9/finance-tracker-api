package com.husnain.financetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

// This is what the client sends when logging in.
// The rules here are basic on purpose, it just checks both fields are actually filled in.
// We don't need to check the email format or password length here, because if they're wrong the login just fails when we check them against the database.
public record LoginRequest(

        // The users email, the example is just what Swagger shows in the docs.
        @Schema(example = "alice@example.com")
        @NotBlank String email,

        // The users password in plain text, it only gets compared to the saved hash and never gets stored.
        @Schema(example = "password123")
        @NotBlank String password

) {
}
