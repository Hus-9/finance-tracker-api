package com.husnain.financetracker.dto;

import com.husnain.financetracker.model.User;

import java.time.Instant;

public record UserResponse(Long id, String email, Instant createdAt) {

    public static UserResponse from(User user){
        return new UserResponse(user.getId(), user.getEmail(), user.getCreatedAt());
    }
}
