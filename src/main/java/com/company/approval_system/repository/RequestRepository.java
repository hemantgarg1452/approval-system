package com.company.approval_system.repository;

import com.company.approval_system.entity.Request;
import com.company.approval_system.enums.RequestStatus;
import com.company.approval_system.enums.RequestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    // Find all requests created by a specific user with pagination
    Page<Request> findByCreatedById(Long userId, Pageable pageable);

    // Find all requests assigned to a specific approver with pagination
    Page<Request> findByApproverId(Long approverId, Pageable pageable);

    // Find pending requests for a specific approver (optimized with composite index)
    Page<Request> findByApproverIdAndStatus(Long approverId, RequestStatus status, Pageable pageable);

    // Find requests by creator and status
    Page<Request> findByCreatedByIdAndStatus(Long userId, RequestStatus status, Pageable pageable);

    // Find requests by type
    List<Request> findByRequestType(RequestType requestType);

    // Count pending requests for an approver
    @Query("SELECT COUNT(r) FROM Request r WHERE r.approver.id = :approverId AND r.status = 'PENDING'")
    long countPendingRequestsByApproverId(@Param("approverId") Long approverId);

    // Find all requests with pagination (admin use case)
    Page<Request> findAll(Pageable pageable);
}
