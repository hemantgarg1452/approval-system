package com.company.approval_system.service;

import com.company.approval_system.dto.UserResponse;
import com.company.approval_system.entity.User;
import com.company.approval_system.exception.ResourceNotFoundException;
import com.company.approval_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    //Get user by ID
    public UserResponse getUserById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("user", "id", id));
        return mapToResponse(user);
    }

    //Get all active users(admin op.)
    @Transactional(readOnly = true)
    public List<UserResponse> getAllActiveUsers(){
        logger.info("Fetching all active users");

        return userRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    //Get subordinates of a manager
    @Transactional(readOnly = true)
    public List<UserResponse>getSubordinates(Long managerId){
        logger.info("Fetching subordinates for manager: {}", managerId);

        return userRepository.findSubordinatesByManagerId(managerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    //Deactivate user (soft delete)
    @Transactional
    public void deactivateUser(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()->new ResourceNotFoundException("User", "id", userId));

        user.setIsActive(false);
        userRepository.save(user);
        logger.info("User deactivated: {}", user.getEmail());
    }

    //Map User entity to UserResponse DTO
    private UserResponse mapToResponse(User user){
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .managerId(user.getManager()!=null ? user.getManager().getId() : null)
                .managerName(user.getManager()!=null ? user.getManager().getFullName() : null)
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
