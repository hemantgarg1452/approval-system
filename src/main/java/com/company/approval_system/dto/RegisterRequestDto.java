package com.company.approval_system.dto;

import com.company.approval_system.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min=8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(min=2, max = 255, message = "Full name must be between 2 and 255 characters")
    private String fullName;

    @NotNull(message = "Role is required")
    private Role role;

    // Manager ID - required for EMPLOYEE role, optional for others
    private Long managerId;
}
