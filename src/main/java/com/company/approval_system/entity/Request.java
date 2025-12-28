package com.company.approval_system.entity;

import com.company.approval_system.enums.RequestStatus;
import com.company.approval_system.enums.RequestType;
import jakarta.persistence.*;

@Entity
@Table(name = "requests")
public class Request {
    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private RequestType requestType;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @ManyToMany
    private User createdBy;
}
