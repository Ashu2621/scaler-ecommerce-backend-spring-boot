package com.scaler.backendproject.service;

import com.scaler.backendproject.dto.AuthResponse;
import com.scaler.backendproject.dto.LoginRequest;
import com.scaler.backendproject.dto.RegisterRequest;
import com.scaler.backendproject.exceptions.ApiException;
import com.scaler.backendproject.exceptions.ConflictException;
import com.scaler.backendproject.models.AppUser;
import com.scaler.backendproject.models.Role;
import com.scaler.backendproject.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("EMAIL_EXISTS", "An account already exists for this email");
        }
        AppUser user = userRepository.save(new AppUser(
                email,
                request.fullName().trim(),
                passwordEncoder.encode(request.password()),
                Set.of(Role.CUSTOMER)
        ));
        return response(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AppUser user = userRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .filter(AppUser::isEnabled)
                .orElseThrow(this::invalidCredentials);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }
        return response(user);
    }

    private AuthResponse response(AppUser user) {
        JwtService.Token token = jwtService.issue(user);
        return new AuthResponse(
                token.value(),
                "Bearer",
                token.expiresAt(),
                user.getId(),
                user.getEmail(),
                user.getRoles().stream().map(Role::name).collect(Collectors.toSet())
        );
    }

    private ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Email or password is incorrect");
    }

    private String normalizeEmail(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
