package com.company.approval_system.controller;

import com.company.approval_system.dto.CreateUserRequest;
import com.company.approval_system.dto.UserResponse;
import com.company.approval_system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;

    @PostMapping
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request){
        return userService.createUser(request);
    }

    @PutMapping("/{id}/deactivate")
    public void deactivate(@PathVariable Long id){
        userService.deactivateUser(id);
    }
}
