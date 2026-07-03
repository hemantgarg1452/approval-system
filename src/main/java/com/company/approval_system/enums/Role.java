package com.company.approval_system.enums;
/**
 * Defines user roles in the approval system hierarchy.
 * Each role has cumulative permissions (ADMIN includes MANAGER and EMPLOYEE permissions).
 */
public enum Role {
    // Base role - can create and view own requests

    EMPLOYEE,

    // Can approve/reject requests from team members

    MANAGER,

    // Full system access including user management

    ADMIN
}