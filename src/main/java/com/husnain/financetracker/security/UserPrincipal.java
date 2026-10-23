package com.husnain.financetracker.security;

import com.husnain.financetracker.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// This is the logged in user in a form Spring Security understands.
// It takes our User entity and copies out just the bits security needs so we're not carrying the whole database entity around.
// This is the object you get from @AuthenticationPrincipal in the controllers which is why user.getId() works there.
// @Getter is Lombok making getId, getEmail and getPasswordHash for us.
@Getter

public class UserPrincipal implements UserDetails{

    private final Long id;
    private final String email;
    private final String passwordHash;

    // Makes a UserPrincipal from a User, CustomUserDetailsService calls this after it finds the user in the database.
    public UserPrincipal(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
    }

    // Spring Security asks for a username, we log in with email so we just give it the email.
    @Override
    public String getUsername() {

        return email;

    }

    // Spring Security asks for the password but we only have the hash which is what it wants anyway.
    // At login it hashes what the user types with BCrypt and checks it matches this.
    @Override
    public String getPassword() {

        return passwordHash;

    }

    // This is the user's roles, everyone just gets ROLE_USER because the app doesn't have admins or anything like that.
    // The ROLE_ bit at the start is how Spring Security knows it's a role.
    // The other UserDetails methods like isEnabled already return true by default so we don't need to write them.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return List.of(new SimpleGrantedAuthority("ROLE_USER"));

    }
}
