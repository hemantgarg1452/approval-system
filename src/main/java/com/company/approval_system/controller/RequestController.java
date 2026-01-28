package com.company.approval_system.controller;

import com.company.approval_system.dto.CreateRequestRequest;
import com.company.approval_system.service.RequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/requests")
@PreAuthorize("hasRole('EMPLOYEE')")
@RequiredArgsConstructor
public class RequestController {
    private final RequestService requestService;

    @PostMapping
    public void create(@Valid @RequestBody CreateRequestRequest request){
        requestService.createRequest(request);
    }
}
