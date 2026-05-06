package com.diegchav.staywise.service;

import com.diegchav.staywise.api.dto.AuthResponse;
import com.diegchav.staywise.api.dto.LoginRequest;
import com.diegchav.staywise.api.dto.RegisterRequest;
import com.diegchav.staywise.domain.entity.Role;
import com.diegchav.staywise.domain.entity.User;
import com.diegchav.staywise.exception.EmailAlreadyRegisteredException;
import com.diegchav.staywise.exception.InvalidCredentialsException;
import com.diegchav.staywise.exception.UserAlreadyTakenException;
import com.diegchav.staywise.repository.UserRepository;
import com.diegchav.staywise.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyTakenException("Username already taken");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyRegisteredException("Email already registered");
        }

        var user = new User(
                request.username(),
                request.email(),
                passwordEncoder.encode(request.password()),
                Role.ROLE_USER
        );

        userRepository.save(user);

        var userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole().name())
                .build();

        var token = jwtService.generateToken(userDetails);

        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        var user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        var userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole().name())
                .build();

        var token = jwtService.generateToken(userDetails);

        return new AuthResponse(token);
    }
}
