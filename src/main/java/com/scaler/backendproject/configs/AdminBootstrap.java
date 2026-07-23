package com.scaler.backendproject.configs;

import com.scaler.backendproject.models.AppUser;
import com.scaler.backendproject.models.Role;
import com.scaler.backendproject.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminBootstrap(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          @Value("${app.bootstrap-admin.email:}") String email,
                          @Value("${app.bootstrap-admin.password:}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) {
            return;
        }
        if (password.length() < 10) {
            throw new IllegalStateException("Bootstrap admin password must contain at least 10 characters");
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (!userRepository.existsByEmailIgnoreCase(normalized)) {
            userRepository.save(new AppUser(
                    normalized,
                    "Platform Administrator",
                    passwordEncoder.encode(password),
                    Set.of(Role.ADMIN, Role.CUSTOMER)
            ));
            log.info("Bootstrap administrator created for {}", normalized);
        }
    }
}
