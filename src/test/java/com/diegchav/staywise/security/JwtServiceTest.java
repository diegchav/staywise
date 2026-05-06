package com.diegchav.staywise.security;

import com.diegchav.staywise.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        var jwtProperties = new JwtProperties();

        jwtProperties.setSecret("my-test-secret-key-that-is-32-chars");
        jwtProperties.setExpiration(1000 * 60 * 60); // 1 hour

        jwtService = new JwtService(jwtProperties);

        userDetails = new User(
                "testuser",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void shouldGenerateValidToken() {
        var token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void shouldExtractUsernameFromToken() {
        var token = jwtService.generateToken(userDetails);
        var username = jwtService.extractUsername(token);

        assertEquals("testuser", username);
    }

    @Test
    void shouldValidateTokenForCorrectUser() {
        var token = jwtService.generateToken(userDetails);

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void shouldInvalidateTokenForDifferentUser() {
        var token = jwtService.generateToken(userDetails);
        var otherUser = new User(
                "otheruser",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        assertFalse(jwtService.isTokenValid(token, otherUser));
    }

    @Test
    void shouldIncludeExtraClaims() {
        Map<String, Object> extraClaims = Map.of("role", "ROLE_ADMIN");
        String token = jwtService.generateToken(extraClaims, userDetails);

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }
}
