package com.company.approval_system.service;

import com.company.approval_system.dto.CreateUserRequest;
import com.company.approval_system.dto.UserResponse;
import com.company.approval_system.entity.Role;
import com.company.approval_system.entity.User;
import com.company.approval_system.repository.RoleRepository;
import com.company.approval_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public UserResponse createUser(CreateUserRequest request){
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);


        Set<Role> roles = request.getRoles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(()->new RuntimeException("Role Not Found")))
                .collect(Collectors.toSet());

        user.setRoles(roles);
        userRepository.save(user);
        return mapToResponse(user);
    }

    @Override
    public void deactivateUser(long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("User Not Found"));
        user.setActive(false);
        userRepository.save(user);
    }

    private UserResponse mapToResponse(User user){
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setActive(user.isActive());
        response.setRoles(
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet())
        );
        return response;
    }
}













