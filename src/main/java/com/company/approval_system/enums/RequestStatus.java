package com.company.approval_system.enums;

// Lifecycle states of a request

public enum RequestStatus {
    PENDING,    // Awaiting approval
    APPROVED,   // Approved by manager
    REJECTED    // Rejected by manager
}