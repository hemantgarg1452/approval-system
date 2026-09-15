# Internal Approval & Request System

A **production-ready backend** for managing internal company approval workflows. Employees raise requests (leave, expense, asset, travel), managers approve or reject them - with a complete audit trail, JWT security, and role-based access control.

**Live Demo:** `https://approval-system-production-0267.up.railway.app`

---

## Table of Contents
- [Problem Statement](#problem-statement)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Features](#features)
- [Database Schema](#database-schema)
- [API Reference](#api-reference)
- [Security Model](#security-model)
- [Project Structure](#project-structure)
- [Local Setup](#local-setup)
- [Deployment](#deployment)
- [CI/CD Pipeline](#cicd-pipeline)
- [Testing](#testing)

---

## Problem Statement

Most companies manage approvals over email chains - requests get lost, there's no visibility into status, no audit trail for compliance, and no way to enforce hierarchy. This system solves that with a centralized, traceable, role-enforced approval workflow.

---

## Architecture

```
Client (REST / Swagger UI)
        │
        ▼
Spring Security Filter Chain
  ├── JwtAuthenticationFilter
  ├── JwtAuthenticationEntryPoint
  └── CustomAccessDeniedHandler
        │
        ▼
Controller Layer  →  Service Layer  →  Repository Layer  →  MySQL
(HTTP only)          (business logic)    (JPA queries)      (Flyway managed)
```

**Request lifecycle:**
```
PENDING  →  APPROVED
         →  REJECTED
```

No other transitions exist. Once approved or rejected, a request cannot be changed - only a new request can be created.

---

## Tech Stack

| Concern | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0 |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| ORM | Spring Data JPA + Hibernate |
| Database | MySQL 8.0 |
| Migrations | Flyway |
| Build | Maven |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Logging | SLF4J + Logback |
| Monitoring | Spring Actuator |
| Testing | JUnit 5 + Testcontainers (real MySQL) |
| CI | GitHub Actions |
| Deployment | Docker + Railway |

---

## Features

**Authentication & Authorization**
- JWT-based stateless authentication
- BCrypt password hashing
- Role-based access control: `EMPLOYEE`, `MANAGER`, `ADMIN`
- Correct HTTP semantics: 401 (unauthenticated) vs 403 (unauthorized)
- Secure admin bootstrap via environment variable - no credentials committed to git

**Request Management**
- Create requests: `LEAVE`, `EXPENSE`, `ASSET`, `TRAVEL`
- Automatic approver assignment based on reporting hierarchy
- Request-type specific validation (leave needs dates, expense needs amount)
- Pagination and sorting on all list endpoints

**Audit & Compliance**
- Immutable `ApprovalHistory` table - every approval action is recorded
- Soft delete for users - historical requests remain intact
- No double-approve: attempting to approve an already-approved request returns `400`

**Operations**
- `/actuator/health` and `/actuator/metrics` endpoints
- Structured logging (INFO for business events, ERROR for failures)
- Profile-based config: `dev` (verbose, Swagger on) vs `prod` (minimal, Swagger off)
- Flyway migrations - schema versioned alongside code

---

## Database Schema

```
┌─────────────┐         ┌──────────────┐         ┌──────────────────┐
│    users    │         │   requests   │         │ approval_history  │
│─────────────│         │──────────────│         │──────────────────│
│ id (PK)     │──┐  ┌──▶│ id (PK)      │────────▶│ id (PK)          │
│ email       │  │  │  │ request_type │         │ request_id (FK)  │
│ password_   │  │  │  │ title        │         │ approver_id (FK) │
│   hash      │  └──│──▶│ created_by   │         │ action           │
│ full_name   │     │  │ approver_id  │         │ comments         │
│ role        │     └──▶│ status       │         │ action_timestamp │
│ manager_id  │──┐      │ amount       │         └──────────────────┘
│ is_active   │  │      │ start_date   │
└─────────────│  │      │ end_date     │
              └──┘      └──────────────┘
         (self-ref:
          manager)
```

**Key decisions:**
- `manager_id` is self-referencing - enables org hierarchy without extra tables
- `is_active` instead of DELETE - soft delete preserves audit trail
- Composite index on `(status, approver_id)` - optimizes "pending requests for me" query
- `approval_history` is append-only - never updated, only inserted

---

## API Reference

**Base URL:** `https://approval-system-production-0267.up.railway.app`  
**Interactive docs:** `/swagger-ui.html` (dev only)

### Authentication

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/auth/login` | Public | Get JWT token |

### Users

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/users` | ADMIN | Create new user |
| GET | `/api/v1/users/me` | Any | Get current user |
| GET | `/api/v1/users` | ADMIN | List all active users |
| GET | `/api/v1/users/{id}` | ADMIN, MANAGER | Get user by ID |
| GET | `/api/v1/users/subordinates` | MANAGER, ADMIN | Get direct reports |
| DELETE | `/api/v1/users/{id}` | ADMIN | Deactivate user |

### Requests

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/requests` | Any | Create request |
| GET | `/api/v1/requests/my-requests` | Any | Own requests (paginated) |
| GET | `/api/v1/requests/pending` | MANAGER, ADMIN | Pending approvals (paginated) |
| GET | `/api/v1/requests` | ADMIN | All requests (paginated) |
| GET | `/api/v1/requests/{id}` | Any | Get request detail |
| PUT | `/api/v1/requests/{id}/approve` | MANAGER, ADMIN | Approve request |
| PUT | `/api/v1/requests/{id}/reject` | MANAGER, ADMIN | Reject request |

### Sample Request/Response

**Login:**
```json
POST /api/v1/auth/login
{
  "email": "manager@company.com",
  "password": "yourpassword"
}

// Response 200
{
  "token": "eyJhbGci...",
  "type": "Bearer",
  "userId": 2,
  "email": "manager@company.com",
  "role": "MANAGER"
}
```

**Create Leave Request:**
```json
POST /api/v1/requests
Authorization: Bearer eyJhbGci...
{
  "requestType": "LEAVE",
  "title": "Annual vacation",
  "description": "Family trip",
  "startDate": "2026-08-01",
  "endDate": "2026-08-10"
}

// Response 201
{
  "id": 1,
  "status": "PENDING",
  "approverId": 2,
  "approverName": "Jane Manager",
  ...
}
```

**Approve Request:**
```json
PUT /api/v1/requests/1/approve
Authorization: Bearer eyJhbGci...
{
  "action": "APPROVED",
  "comments": "Approved, enjoy your leave"
}

// Response 200
{
  "id": 1,
  "status": "APPROVED",
  ...
}
```

**Error responses are consistent across all endpoints:**
```json
{
  "timestamp": "2026-08-01T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have permission to access this resource.",
  "path": "/api/v1/requests/1/approve"
}
```

---

## Security Model

```
Three-layer defense:

Layer 1 - Filter (JwtAuthenticationFilter)
  Every request → extract token → validate → set SecurityContext
  No token / invalid token → JwtAuthenticationEntryPoint → 401

Layer 2 - Config (SecurityConfig @PreAuthorize)
  Valid token, wrong role → CustomAccessDeniedHandler → 403

Layer 3 - Service (RequestService business rules)
  Valid role, wrong ownership → UnauthorizedException → 403
  (e.g. Manager A trying to approve Manager B's team's request)
```

**Why three layers?**
Layer 2 alone would mean a MANAGER with a valid token can call the endpoint. Layer 3 catches that the MANAGER isn't the *assigned* approver for that specific request. Role check and ownership check are different concerns.

**Admin bootstrap:**  
First admin is created at startup via `AdminBootstrapRunner` reading from environment variables. No registration endpoint exists - all user creation is ADMIN-only via `POST /api/v1/users`. No credentials are ever committed to git.

---

## Project Structure

```
src/
├── main/java/com/company/approval_system/
│   ├── config/
│   │   ├── SecurityConfig.java          # JWT filter chain, role rules
│   │   ├── AdminBootstrapRunner.java    # First admin from env vars
│   │   ├── SwaggerConfig.java
│   │   └── CorsConfig.java
│   ├── controller/                      
│   │   ├── AuthController.java          # Login
│   │   ├── UserController.java
│   │   └── RequestController.java
│   ├── service/                         
│   │   ├── AuthService.java
│   │   ├── UserService.java
│   │   └── RequestService.java
│   ├── repository/                      # Spring Data JPA interfaces
│   ├── entity/                          # JPA entities
│   ├── dto/request/                     # What client sends
│   ├── dto/response/                    # What client receives
│   ├── security/
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── JwtAuthenticationEntryPoint.java  # 401 handler
│   │   ├── CustomAccessDeniedHandler.java    # 403 handler
│   │   ├── UserPrincipal.java
│   │   └── CustomUserDetailsService.java
│   ├── enums/                           # Role, RequestType, RequestStatus
│   └── exception/
│       ├── GlobalExceptionHandler.java  # @ControllerAdvice for app exceptions
│       ├── ResourceNotFoundException.java
│       ├── UnauthorizedException.java
│       └── InvalidRequestException.java
└── resources/
    ├── application.yml                  # Shared base config
    ├── application-dev.yml              
    ├── application-prod.yml             
    └── db/migration/
        └── V1__init_schema.sql          
```

---

## Local Setup

### Prerequisites
- Java 17+
- Docker Desktop (running)
- Maven 3.9+

### 1. Clone and configure

```bash
git clone https://github.com/yourusername/approval-system.git
cd approval-system
```

Create `.env` in project root (never commit this):
```env
MYSQL_ROOT_PASSWORD=change_it
MYSQL_DATABASE=approval_system
MYSQL_USER=approval_app
MYSQL_PASSWORD=yourpassword

SPRING_PROFILES_ACTIVE=dev
JWT_SECRET=your-minimum-256-bit-secret-key-here-change-this
JWT_EXPIRATION_MS=86400000

APP_ADMIN_EMAIL=admin@company.com
APP_ADMIN_PASSWORD=adminPass1
APP_ADMIN_FULLNAME=System Administrator
```

### 2. Start

```bash
docker compose --env-file .env up --build
```

### 3. Verify

```bash
# Health check
curl http://localhost:8080/actuator/health
# → {"status":"UP"}
```

### 4. First login

Admin is auto-created on first startup from your `.env` values:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@company.com","password":"adminPass1"}'
```

---

## Deployment

Deployed on **Railway** with two services: App + MySQL.

### Environment variables set in Railway dashboard

```env
SPRING_PROFILES_ACTIVE=prod
DB_URL=${{MySQL.MYSQL_URL}}
DB_USERNAME=${{MySQL.MYSQLUSER}}
DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
JWT_SECRET=<generated secret>
APP_ADMIN_EMAIL=admin@company.com
APP_ADMIN_PASSWORD=<secure password>
```

Railway injects MySQL connection variables automatically via service references (`${{MySQL.MYSQL_URL}}`). No hardcoded credentials anywhere.

### Deployment flow

```
Push to main
     ↓
GitHub Actions CI runs (build + tests)
     ↓
Railway auto-deploys from main branch
     ↓
Docker image built
     ↓
Flyway migrations run on startup
     ↓
AdminBootstrapRunner creates admin if not exists
     ↓
App healthy at /actuator/health
```

---

## CI/CD Pipeline

**GitHub Actions** runs on every push and every PR to main.

```yaml
Trigger: push (all branches) / PR to main
Runner:  ubuntu-latest
Steps:
  1. Checkout code
  2. Set up Java 17 (Temurin, Maven cache)
  3. mvn verify  ← builds + runs all tests
  4. Upload surefire reports (on failure only)
```

Typical run time: **~1m 45s** (Maven cache warm).

The pipeline catches:
- Compilation errors
- Failed unit tests (RequestServiceTest - mocked)
- Failed integration tests (RequestApprovalFlowIntegrationTest - real MySQL via Testcontainers)
- Schema/migration issues (Flyway runs against a real container in CI)
- Context load failures (JWT config, bean wiring)

---

## Testing

### Test strategy

| Type | Class | What it tests | Database |
|---|---|---|---|
| Unit | `RequestServiceTest` | Business logic in isolation | Mocked |
| Integration | `RequestApprovalFlowIntegrationTest` | Full stack: HTTP → Security → Service → DB | Real MySQL (Testcontainers) |

### Why Testcontainers, not H2

H2 is an in-memory database that behaves slightly differently from MySQL - `ENUM` types, collations, and FK enforcement differ. If tests pass on H2 but the schema uses MySQL-specific features, you only find out at deployment. Testcontainers spins a real `mysql:8.0` container (same image as production) so what passes in CI is proven to work in prod.

### Run tests

```bash
# All tests
mvn test

# Integration tests only
mvn test -Dtest=RequestApprovalFlowIntegrationTest
```

Docker Desktop must be running for integration tests.

### Test coverage

| Scenario | Test |
|---|---|
| Employee creates request, manager approves | ✅ Happy path |
| Employee tries to approve (wrong role) | ✅ 403 expected |
| Manager approves same request twice | ✅ 400 expected |
| Request without token | ✅ 401 expected |
| App context loads with real DB + Flyway | ✅ contextLoads |

---

## Health & Monitoring

```bash
# Health (public)
GET /actuator/health

# Metrics (authenticated in prod)
GET /actuator/metrics
GET /actuator/metrics/jvm.memory.used
```

---

## What's Next (Roadmap)

- [ ] Rate limiting on login endpoint (brute force protection)
- [ ] Refresh token + logout (token revocation)
- [ ] Email notifications on approval/rejection
- [ ] SonarCloud integration in CI (code quality)
- [ ] Multi-level approval workflows
- [ ] File attachment support for expense requests

---

## Key Engineering Decisions

| Decision | Why                                                                                                                  |
|---|----------------------------------------------------------------------------------------------------------------------|
| `ddl-auto: validate` everywhere | Hibernate should never alter schema in prod - Flyway owns it                                                         |
| Flyway, not `schema.sql` | Versioned, tracked, idempotent migrations vs one flat file with no history                                           |
| Testcontainers, not H2 | Same MySQL version in tests as in prod - catches real schema issues                                                  |
| `AuthenticationEntryPoint` + `AccessDeniedHandler` | Spring Security exceptions never reach `@ControllerAdvice` - dedicated handlers needed for correct 401/403 semantics |
| `CommandLineRunner` for admin | Credentials from env vars at runtime, never committed to git                                                         |
| Soft delete (`is_active`) | Deleting users would orphan historical requests and break audit trail                                                |
| DTO pattern everywhere | Entities never leave the service layer - API contract is independent of DB schema                                    |