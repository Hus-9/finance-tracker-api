package com.husnain.financetracker.service;

import com.husnain.financetracker.dto.UserResponse;
import com.husnain.financetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor

public class UserService {

    private final UserRepository userRepository;

    public UserResponse getProfile(Long userId) {

        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    }
}
