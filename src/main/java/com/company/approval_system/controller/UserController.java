package com.company.approval_system.controller;

import com.company.approval_system.dto.UserResponse;
import com.company.approval_system.security.UserPrincipal;
import com.company.approval_system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//Handles user related endpoints.
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Users", description = "User management operations")
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get details of the authenticated user")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser){
        UserResponse response = userService.getUserById(currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get user by ID", description = "Get user details by ID (Admin/Manager only)")
    public ResponseEntity<UserResponse>getUserById(@PathVariable Long id){
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Get all active users(Admin only)")
    public ResponseEntity<List<UserResponse>>getAllUsers(){
        List<UserResponse> users = userService.getAllActiveUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/subordinates")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Get subordinates", description = "Get all subordinates of the current manager")
    public ResponseEntity<List<UserResponse>> getSubordinates(@AuthenticationPrincipal UserPrincipal currentUser){
        List<UserResponse> subordinates = userService.getSubordinates(currentUser.getId());
        return ResponseEntity.ok(subordinates);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate user", description = "Soft delete user (Admin only)")
    public ResponseEntity<Void>deactivateUser(@PathVariable Long id){
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }
}
