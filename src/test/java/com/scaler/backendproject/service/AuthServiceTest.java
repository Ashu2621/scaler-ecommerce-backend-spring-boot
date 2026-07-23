package com.scaler.backendproject.service;

import com.scaler.backendproject.dto.LoginRequest;
import com.scaler.backendproject.exceptions.ApiException;
import com.scaler.backendproject.models.AppUser;
import com.scaler.backendproject.models.Role;
import com.scaler.backendproject.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void authenticatesValidPasswordAndReturnsBearerToken() {
        AppUser user = new AppUser("buyer@example.com", "Buyer", "hash", Set.of(Role.CUSTOMER));
        when(userRepository.findByEmailIgnoreCase("buyer@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@1", "hash")).thenReturn(true);
        when(jwtService.issue(user)).thenReturn(new JwtService.Token(
                "signed-token", Instant.parse("2030-01-01T00:00:00Z")));

        var response = authService.login(new LoginRequest("BUYER@example.com", "Password@1"));

        assertThat(response.accessToken()).isEqualTo("signed-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.roles()).containsExactly("CUSTOMER");
    }

    @Test
    void rejectsInvalidPasswordWithoutLeakingWhichFieldFailed() {
        AppUser user = new AppUser("buyer@example.com", "Buyer", "hash", Set.of(Role.CUSTOMER));
        when(userRepository.findByEmailIgnoreCase("buyer@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("buyer@example.com", "wrong")))
                .isInstanceOf(ApiException.class)
                .hasMessage("Email or password is incorrect");
    }
}
