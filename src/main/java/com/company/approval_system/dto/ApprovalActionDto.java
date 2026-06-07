package com.company.approval_system.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalActionDto {

    @NotNull(message = "Action is required")
    private ApprovalActionDto action;

    @Size(max = 1000, message = "Comments cannot exceed 1000 characters")
    private String comments;
}