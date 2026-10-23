package com.husnain.financetracker.service;

import com.husnain.financetracker.dto.UserResponse;
import com.husnain.financetracker.exception.ResourceNotFoundException;
import com.husnain.financetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// This is where the logic for user stuff happens, UserController passes everything in here.
@Service
@RequiredArgsConstructor

public class UserService {

    private final UserRepository userRepository;

    // Gets the logged in user's profile using the id from their token.
    // findById comes free from JpaRepository and gives back an Optional so if the user is found it gets turned into a UserResponse.
    // The not found error should basically never happen because the JWT filter already checked the user exists.
    // It's still there in case the user gets deleted in the tiny gap between the filter and this running
    public UserResponse getProfile(Long userId) {

        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    }
}
