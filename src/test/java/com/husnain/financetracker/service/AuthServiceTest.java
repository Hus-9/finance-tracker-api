package com.husnain.financetracker.service;

import com.husnain.financetracker.dto.RegisterRequest;
import com.husnain.financetracker.dto.UserResponse;
import com.husnain.financetracker.exception.ConflictException;
import com.husnain.financetracker.model.Category;
import com.husnain.financetracker.model.User;
import com.husnain.financetracker.repository.CategoryRepository;
import com.husnain.financetracker.repository.UserRepository;
import com.husnain.financetracker.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Unit tests for signing up, everything AuthService uses is faked.
@ExtendWith(MockitoExtension.class)

public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;

    // A captor grabs whatever got passed into a fake method so the test can look at it afterwards.
    @Captor
    private ArgumentCaptor<User> userCaptor;
    @Captor
    private ArgumentCaptor<List<Category>> categoriesCaptor;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerHashesThePasswordAndGivesTheUserSixDefaultCategories() {

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");

        // Pretends the database gave the new user id 7.
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {

            User user = invocation.getArgument(0);
            user.setId(7L);
            return user;

        });

        UserResponse response = authService.register(new RegisterRequest("Alice@Example.com", "password123"));

        // The email got made lowercase and the hash got saved, never the real password.
        assertThat(response.email()).isEqualTo("alice@example.com");
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("hashed-password");

        // The six default categories got saved for this user.
        verify(categoryRepository).saveAll(categoriesCaptor.capture());
        assertThat(categoriesCaptor.getValue()).extracting(Category::getName)
                .containsExactlyInAnyOrder("Salary", "Rent", "Food", "Transport", "Bills", "Entertainment");

    }

    @Test
    void registerRejectsAnEmailThatsAlreadyTaken() {

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("alice@example.com", "password123")))
                .isInstanceOf(ConflictException.class);

        // Nothing got saved.
        verify(userRepository, never()).save(any());
        verify(categoryRepository, never()).saveAll(any());

    }
}
