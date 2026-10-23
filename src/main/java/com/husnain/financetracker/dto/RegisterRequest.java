package com.husnain.financetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// This is what the client sends when signing up.
// The rules here are stricter than LoginRequest, because this is where the email and password actually get saved.
public record RegisterRequest(

        // The email they want to sign up with.
        // @Email checks it looks like an email, but it doesn't check the email actually exists.
        @Schema(example = "alice@example.com")
        @NotBlank @Email
        String email,

        // The password they want, it gets hashed with BCrypt before it's saved.
        // The minimum is 8 so people can't pick a really short password.
        // The max is 72 because BCrypt only looks at the first 72 bytes so anything after that would just get ignored.
        // For normal letters and numbers 72 bytes is 72 characters but stuff like emojis take up more than 1 byte each.
        @Schema(example = "password123")
        @NotBlank @Size(min = 8, max = 72)
        String password

) {
}
