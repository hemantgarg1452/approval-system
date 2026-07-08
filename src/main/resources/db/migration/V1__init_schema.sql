-- V1: Baseline schema for User, Request, and ApprovalHistory.
-- This is the initial migration for a greenfield project — it establishes
-- the full starting schema in one file. Every schema change after this
-- point (new columns, new indexes, new tables) gets its own V2, V3, ...
-- migration. Never edit this file once it has run against any real database.

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role ENUM('EMPLOYEE', 'MANAGER', 'ADMIN') NOT NULL DEFAULT 'EMPLOYEE',
    manager_id BIGINT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_email (email),
    INDEX idx_manager_id (manager_id),
    INDEX idx_role (role),

    CONSTRAINT fk_user_manager FOREIGN KEY (manager_id)
        REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_type ENUM('LEAVE', 'EXPENSE', 'ASSET', 'TRAVEL') NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    created_by_id BIGINT NOT NULL,
    approver_id BIGINT NOT NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    amount DECIMAL(10, 2) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_created_by (created_by_id),
    INDEX idx_approver (approver_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_status_approver (status, approver_id),

    CONSTRAINT fk_request_creator FOREIGN KEY (created_by_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_request_approver FOREIGN KEY (approver_id)
        REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE approval_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    approver_id BIGINT NOT NULL,
    action ENUM('APPROVED', 'REJECTED') NOT NULL,
    comments TEXT NULL,
    action_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_request_id (request_id),
    INDEX idx_approver_id (approver_id),

    CONSTRAINT fk_approval_request FOREIGN KEY (request_id)
        REFERENCES requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_approval_approver FOREIGN KEY (approver_id)
        REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;