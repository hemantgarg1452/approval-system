package com.company.approval_system.dto;

import com.company.approval_system.enums.RequestStatus;
import com.company.approval_system.enums.RequestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestResponse {
    private Long id;
    private RequestType requestType;
    private String title;
    private String description;
    private RequestStatus status;
    private Long createdById;
    private String createdByName;
    private Long approverId;
    private String approverName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
