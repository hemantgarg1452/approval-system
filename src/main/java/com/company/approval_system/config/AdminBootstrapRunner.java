package com.company.approval_system.config;

import com.company.approval_system.entity.User;
import com.company.approval_system.enums.Role;
import com.company.approval_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;

@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.fullName:System Administrator}")
    private String adminFullName;

    @Override
    public void run(String... args){
        if(userRepository.existsByEmail(adminEmail)){
            logger.info("Admin user already exists, skipping bootstrap");
            return;
        }
        User admin = User.builder()
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .fullName(adminFullName)
                .role(Role.ADMIN)
                .isActive(true)
                .build();
        userRepository.save(admin);

        logger.info("Admin user created successfully with email: {}", adminEmail);
    }

}
