package com.company.approval_system.controller;

import com.company.approval_system.dto.AuthResponse;
import com.company.approval_system.dto.LoginRequestDto;
import com.company.approval_system.dto.RegisterRequestDto;
import com.company.approval_system.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//Handle authentication endpoints

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authenticated and registration")
public class AuthController {

    private final AuthService authService;
    @PostMapping("/login")
    @Operation(summary = "User login",  description = "Authenticate user and receive JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequestDto request){
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Register new user (typically admin-only in production")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequestDto request){
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(201).body(response);
    }

}
