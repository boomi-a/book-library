package com.boomlibrary.service;

import com.boomlibrary.dto.request.LoginRequest;
import com.boomlibrary.dto.request.RegisterRequest;
import com.boomlibrary.dto.response.AuthResponse;
import com.boomlibrary.entity.User;
import com.boomlibrary.repository.UserRepository;
import com.boomlibrary.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authManager;
    private final UserDetailsService userDetailsService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already taken");
        }

        User user = User.builder()
                .email(request.email())
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(User.Role.USER)
                .build();
        userRepository.save(user);

        UserDetails ud = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(ud, user.getRole().name());
        return new AuthResponse(token, user.getRole().name(), user.getUsername());
    }

    public AuthResponse login(LoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserDetails ud = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(ud, user.getRole().name());
        return new AuthResponse(token, user.getRole().name(), user.getUsername());
    }
}
