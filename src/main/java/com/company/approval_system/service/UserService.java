package com.company.approval_system.service;

import com.company.approval_system.dto.CreateUserRequest;
import com.company.approval_system.dto.UserResponse;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    void deactivateUser(long userId);
}
