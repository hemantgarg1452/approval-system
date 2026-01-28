package com.company.approval_system.repository;

import com.company.approval_system.entity.Request;
import com.company.approval_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request>findByCreatedBy(User user);
}
