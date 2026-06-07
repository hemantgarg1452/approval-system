package com.company.approval_system.dto;

import com.company.approval_system.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private Role role;
    private Long managerId;
    private String managerName;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
