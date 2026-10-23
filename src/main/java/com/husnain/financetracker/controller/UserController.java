package com.husnain.financetracker.controller;

import com.husnain.financetracker.dto.UserResponse;
import com.husnain.financetracker.security.UserPrincipal;
import com.husnain.financetracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// This controller is for user stuff, right now it's just the endpoint to get your own profile
// Like the other controllers, it passes the work to UserService.
// @RequestMapping means everything in here starts with /api/users.
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor

public class UserController {

    private final UserService userService;

    // GET /api/users/me gives back the profile of whoever is logged in.
    // It uses the id from the JWT instead of taking one in the URL so there's no way to look up someone else's profile.
    // UserResponse only has the safe fields so the password hash never gets sent back.
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UserPrincipal user) {

        return userService.getProfile(user.getId());

    }
}
