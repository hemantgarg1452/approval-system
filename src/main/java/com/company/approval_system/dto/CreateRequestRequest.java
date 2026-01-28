package com.company.approval_system.dto;

import com.company.approval_system.enums.RequestType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRequestRequest {

    @NotNull
    private RequestType requestType;
}
