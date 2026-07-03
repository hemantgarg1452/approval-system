package com.company.approval_system.repository;

import com.company.approval_system.entity.ApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory, Long> {
    // Find all approval history entries for a specific request
    List<ApprovalHistory> findByRequestIdOrderByActionTimestampDesc(Long requestId);

    // Find all approval actions taken by a specific approver
    List<ApprovalHistory> findByApproverIdOrderByActionTimestampDesc(Long approverId);

}
