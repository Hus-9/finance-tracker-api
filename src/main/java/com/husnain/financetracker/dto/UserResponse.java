package com.husnain.financetracker.dto;

import com.husnain.financetracker.model.User;

import java.time.Instant;

// This is what the API sends back when it returns a user, like after registering or on /api/users/me.
// It only has the id, email and when the account was made so the password hash never leaves the server.
public record UserResponse(Long id, String email, Instant createdAt) {

    // Turns a User from the database into a UserResponse.
    // It's static so you can just call UserResponse.from(user) without making an object first.
    public static UserResponse from(User user){
        return new UserResponse(user.getId(), user.getEmail(), user.getCreatedAt());

    }
}
