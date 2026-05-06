package com.diegchav.staywise.service;

import com.diegchav.staywise.api.dto.LoginRequest;
import com.diegchav.staywise.api.dto.RegisterRequest;
import com.diegchav.staywise.domain.entity.Role;
import com.diegchav.staywise.domain.entity.User;
import com.diegchav.staywise.repository.UserRepository;
import com.diegchav.staywise.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(
                "testuser",
                "test@example.com",
                "encodedPassword",
                Role.ROLE_USER
        );
    }

    @Test
    void shouldRegisterNewUser() {
        var request = new RegisterRequest("testuser", "test@example.com", "password123");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        var response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowWhenUsernameExists() {
        var request = new RegisterRequest("testuser", "test@example.com", "password123");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        var exception = assertThrows(IllegalArgumentException.class, () -> authService.register(request));

        assertEquals("Username already taken", exception.getMessage());
    }

    @Test
    void shouldThrowWhenEmailExists() {
        var request = new RegisterRequest("testuser", "test@example.com", "password123");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        var exception = assertThrows(IllegalArgumentException.class, () -> authService.register(request));

        assertEquals("Email already registered", exception.getMessage());
    }

    @Test
    void shouldLoginUser() {
        var request = new LoginRequest("testuser", "password123");

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        var response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void shouldThrowWhenLoginUserNotFound() {
        var request = new LoginRequest("unknown", "password123");

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        var exception = assertThrows(IllegalArgumentException.class, () -> authService.login(request));

        assertEquals("Invalid credentials", exception.getMessage());
    }
}
