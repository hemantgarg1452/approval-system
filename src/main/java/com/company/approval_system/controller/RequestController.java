package com.company.approval_system.controller;

import com.company.approval_system.dto.ApprovalActionDto;
import com.company.approval_system.dto.CreateRequestDto;
import com.company.approval_system.dto.RequestResponse;
import com.company.approval_system.security.UserPrincipal;
import com.company.approval_system.service.RequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/requests")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Requests", description = "Request management operations")
public class RequestController {
    private final RequestService requestService;

    @PostMapping
    @Operation(summary = "Create request", description = "Create a new approval request")
    public ResponseEntity<RequestResponse> createRequest(
            @Valid @RequestBody CreateRequestDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser){
        RequestResponse response = requestService.createRequest(dto, currentUser);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/my-requests")
    @Operation(summary = "Get my requests", description = "Get all requests created by the current user")
    public ResponseEntity<Page<RequestResponse>> getMyRequests(
           @AuthenticationPrincipal UserPrincipal currentUser,
           @RequestParam(defaultValue = "0") int page,
           @RequestParam(defaultValue = "10") int size,
           @RequestParam(defaultValue = "createdAt") String sortBy,
           @RequestParam(defaultValue = "DESC") Sort.Direction direction){
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<RequestResponse> requests = requestService.getMyRequests(currentUser, pageable);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Get pending requests", description = "Get pending requests assigned to current user for approval")
    public ResponseEntity<Page<RequestResponse>> getPendingRequests(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RequestResponse> requests = requestService.getPendingRequests(currentUser, pageable);
        return ResponseEntity.ok(requests);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all requests", description = "Get all requests in the system (Admin only)")
    public ResponseEntity<Page<RequestResponse>> getAllRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction){

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<RequestResponse> requests = requestService.getAllRequests(pageable);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get request by ID", description = "Get details of a specific request")
    public ResponseEntity<RequestResponse>getRequestById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser){
        RequestResponse response = requestService.getRequestById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Approve request", description = "Approve a pending request")
    public ResponseEntity<RequestResponse> approveRequest(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalActionDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser){
        RequestResponse response = requestService.approveRequest(id, dto, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Reject request", description = "Reject a pending request")
    public ResponseEntity<RequestResponse> rejectRequest(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalActionDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser){
        RequestResponse response = requestService.rejectRequest(id, dto, currentUser);
        return ResponseEntity.ok(response);
    }
}
