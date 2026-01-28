package com.company.approval_system.service;

import com.company.approval_system.dto.CreateRequestRequest;
import com.company.approval_system.entity.Request;
import com.company.approval_system.entity.User;
import com.company.approval_system.enums.RequestStatus;
import com.company.approval_system.repository.RequestRepository;
import com.company.approval_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService{
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    @Override
    public void createRequest(CreateRequestRequest dto){
        String email =
                Objects.requireNonNull(SecurityContextHolder.getContext()
                                .getAuthentication())
                        .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(()->new RuntimeException("User not found"));

        Request request = new Request();
        request.setRequestType(dto.getRequestType());
        request.setStatus(RequestStatus.PENDING);
        request.setCreatedBy(user);
        request.setCreatedAt(LocalDateTime.now());

        requestRepository.save(request);
    }
}
