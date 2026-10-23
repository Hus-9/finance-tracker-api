package com.husnain.financetracker.security;

import com.husnain.financetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// This tells Spring Security how to find a user in our database.
// Spring Security calls it when someone logs in, it gets the user then checks the password against the saved hash itself.
// @Service makes it a Spring bean so Spring Security finds it and uses it automatically.
@Service
@RequiredArgsConstructor

public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Spring Security calls this username but in our app people log in with their email so that's what gets passed in.
    // If the user is found it gets wrapped in a UserPrincipal which is the user object Spring Security knows how to work with.
    // If there's no user with that email it throws UsernameNotFoundException.
    // Spring Security swaps that for a normal bad credentials error so a wrong email and a wrong password look exactly the same to the client.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
