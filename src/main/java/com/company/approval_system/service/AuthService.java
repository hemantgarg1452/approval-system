package com.company.approval_system.service;

import com.company.approval_system.dto.AuthResponse;
import com.company.approval_system.dto.LoginRequestDto;
import com.company.approval_system.dto.RegisterRequestDto;
import com.company.approval_system.entity.User;
import com.company.approval_system.exception.InvalidRequestException;
import com.company.approval_system.exception.ResourceNotFoundException;
import com.company.approval_system.repository.UserRepository;
import com.company.approval_system.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    //Authenticate user and generate JWT token

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequestDto request){
        logger.info("Login attempt for user: {}", request.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        //Generate JWT token
        String token = jwtTokenProvider.generateToken(authentication);

        // Retrieve user details for response
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        logger.info("User logged in successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    //Register new user (admin-only operation in production)
    @Transactional
    public AuthResponse register(RegisterRequestDto request) {
        logger.info("Registration request for email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new InvalidRequestException("Email already registered: " + request.getEmail());
        }

        // Validate manager assignment for employees
        User manager = null;
        if (request.getManagerId() != null) {
            manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + request.getManagerId()));
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(request.getRole())
                .manager(manager)
                .isActive(true)
                .build();

        user = userRepository.save(user);

        logger.info("User registered successfully: {}", user.getEmail());

        // Auto-login after registration
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token = jwtTokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
}


