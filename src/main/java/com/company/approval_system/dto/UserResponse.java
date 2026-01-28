package com.company.approval_system.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@RequiredArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private boolean active;
    private Set<String> roles;
}
