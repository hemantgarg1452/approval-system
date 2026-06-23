package com.company.approval_system.service;

import com.company.approval_system.dto.ApprovalActionDto;
import com.company.approval_system.dto.CreateRequestDto;
import com.company.approval_system.dto.RequestResponse;
import com.company.approval_system.entity.ApprovalHistory;
import com.company.approval_system.entity.Request;
import com.company.approval_system.entity.User;
import com.company.approval_system.enums.ApprovalAction;
import com.company.approval_system.enums.RequestStatus;
import com.company.approval_system.enums.RequestType;
import com.company.approval_system.exception.InvalidRequestException;
import com.company.approval_system.exception.ResourceNotFoundException;
import com.company.approval_system.exception.UnauthorizedException;
import com.company.approval_system.repository.ApprovalHistoryRepository;
import com.company.approval_system.repository.RequestRepository;
import com.company.approval_system.repository.UserRepository;
import com.sun.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class RequestService {
    private static final Logger logger = LoggerFactory.getLogger(RequestService.class);

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;

    //create a new request
    public RequestResponse createRequest(CreateRequestDto dto, UserPrincipal currentUser){
        logger.info("Creating request for user: {}", currentUser.getEmail());

        //Validate request-specific fields
        // Validate request-specific fields
        validateRequestFields(dto);

        User creator = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        // Determine approver (user's manager)
        User approver = creator.getManager();
        if (approver == null) {
            throw new InvalidRequestException("Cannot create request: No manager assigned to your account");
        }

        // Create request entity
        Request request = Request.builder()
                .requestType(dto.getRequestType())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(RequestStatus.PENDING)
                .createdBy(creator)
                .approver(approver)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .amount(dto.getAmount())
                .build();

        request = requestRepository.save(request);

        logger.info("Request created with ID: {} for user: {}", request.getId(), creator.getEmail());

        return mapToResponse(request);
    }
    //Get requests created by the current user
    @Transactional(readOnly = true)
    public Page<RequestResponse> getMyRequests(UserPrincipal currentUser, Pageable pageable) {
        return requestRepository.findByCreatedById(currentUser.getId(), pageable)
                .map(this::mapToResponse);
    }

    //Get pending requests for current user (if they are manager)
    @Transactional(readOnly = true)
    public Page<RequestResponse> getPendingRequests(UserPrincipal currentUser, Pageable pageable) {
        return requestRepository.findByApproverIdAndStatus(
                currentUser.getId(),
                RequestStatus.PENDING,
                pageable
        ).map(this::mapToResponse);
    }

    //Get all requests(admin only)
    @Transactional(readOnly = true)
    public Page<RequestResponse> getAllRequests(Pageable pageable) {
        return requestRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    //Get request by ID with authorization check
    @Transactional(readOnly = true)
    public RequestResponse getRequestById(Long requestId, UserPrincipal currentUser) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request", "id", requestId));

        // Authorization: user can view if they created it or are the approver
        if (!request.getCreatedBy().getId().equals(currentUser.getId()) &&
                !request.getApprover().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You do not have permission to view this request");
        }

        return mapToResponse(request);
    }

    //Approve a request
    @Transactional
    public RequestResponse approveRequest(Long requestId, ApprovalActionDto dto, UserPrincipal currentUser) {
        return processApprovalAction(requestId, ApprovalAction.APPROVED, dto.getComments(), currentUser);
    }

    //Reject a request
    @Transactional
    public RequestResponse rejectRequest(Long requestId, ApprovalActionDto dto, UserPrincipal currentUser) {
        return processApprovalAction(requestId, ApprovalAction.REJECTED, dto.getComments(), currentUser);
    }

    //Common logic for approval/rejection
    private RequestResponse processApprovalAction(Long requestId, ApprovalAction action,
                                                  String comments, UserPrincipal currentUser) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request", "id", requestId));

        // Validate request is in PENDING status
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidRequestException("Cannot " + action.name().toLowerCase() +
                    " request: Request is already " + request.getStatus());
        }

        // Validate user is the assigned approver
        if (!request.getApprover().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to " +
                    action.name().toLowerCase() + " this request");
        }

        // Update request status
        RequestStatus newStatus = (action == ApprovalAction.APPROVED) ?
                RequestStatus.APPROVED : RequestStatus.REJECTED;
        request.setStatus(newStatus);
        request = requestRepository.save(request);

        // Create approval history entry
        User approver = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        ApprovalHistory history = ApprovalHistory.builder()
                .request(request)
                .approver(approver)
                .action(action)
                .comments(comments)
                .build();

        approvalHistoryRepository.save(history);

        logger.info("Request {} {} by user: {}", requestId, action.name(), currentUser.getEmail());

        return mapToResponse(request);
    }

    //Validate request-specific required fields based on request type
    private void validateRequestFields(CreateRequestDto dto) {
        RequestType type = dto.getRequestType();

        if ((type == RequestType.LEAVE || type == RequestType.TRAVEL)) {
            if (dto.getStartDate() == null || dto.getEndDate() == null) {
                throw new InvalidRequestException(
                        type.name() + " requests require start date and end date");
            }
            if (dto.getEndDate().isBefore(dto.getStartDate())) {
                throw new InvalidRequestException("End date cannot be before start date");
            }
        }

        if (type == RequestType.EXPENSE) {
            if (dto.getAmount() == null) {
                throw new InvalidRequestException("EXPENSE requests require an amount");
            }
        }
    }

    //Map request entity to RequestResponse Dto
    private RequestResponse mapToResponse(Request request) {
        return RequestResponse.builder()
                .id(request.getId())
                .requestType(request.getRequestType())
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus())
                .createdById(request.getCreatedBy().getId())
                .createdByName(request.getCreatedBy().getFullName())
                .approverId(request.getApprover().getId())
                .approverName(request.getApprover().getFullName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .amount(request.getAmount())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}

