package com.company.approval_system.repository;

import com.company.approval_system.entity.User;
import com.company.approval_system.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByIsActiveTrue();
    List<User> findByRole(Role role);

    // Find all subordinates (direct reports) of a manager

    @Query("SELECT u FROM User u WHERE u.manager.id = :managerId AND u.isActive = true")
    List<User>findSubordinatesByManagerId(@Param("managerId") Long managerId);

    // Find active users by role
    List<User> findByRoleAndIsActiveTrue(Role role);
}
