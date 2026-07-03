# Internal Approval & Request System

A production-ready backend system for managing internal approval workflows within organizations. Employees can submit requests (leave, expense, asset, travel), and managers can approve or reject them with complete audit trails.

## 🎯 Problem Statement

Organizations struggle with manual approval processes:
- Email chains create delays and confusion
- No centralized tracking or visibility
- Lost requests and unclear status
- No audit trail for compliance
- Difficult to enforce approval hierarchies

This system solves these problems with a digital, traceable, and efficient approval workflow.

---

## 🏗️ Architecture

### High-Level Architecture
```
Client (Web/Mobile) → Security Filter (JWT) → Controller → Service → Repository → MySQL
```

### Technology Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.0 |
| Security | Spring Security + JWT |
| ORM | Spring Data JPA + Hibernate |
| Database | MySQL 8.0 |
| Build | Maven |
| API Docs | Swagger/OpenAPI |
| Logging | SLF4J + Logback |
| Monitoring | Spring Actuator |
| Testing | JUnit 5 + Mockito |
| Containerization | Docker + Docker Compose |

---

## 📊 Database Schema

### Users Table
- **Purpose**: Store user credentials and organizational hierarchy
- **Key Fields**: id, email, password_hash, role, manager_id
- **Relationships**: Self-referencing (manager), one-to-many (requests)

### Requests Table
- **Purpose**: Store approval requests
- **Key Fields**: id, request_type, status, created_by_id, approver_id
- **Relationships**: Many-to-one (user), one-to-many (approval_history)

### Approval History Table
- **Purpose**: Immutable audit log of all approval actions
- **Key Fields**: id, request_id, approver_id, action, comments

**ER Diagram:**
```
User (1) ←─── (N) Request (1) ←─── (N) ApprovalHistory
  │                    │
  └──── manager_id ────┘
```

---

## 🔐 Security Model

### Authentication Flow
1. User logs in with email/password → POST `/api/v1/auth/login`
2. Server validates credentials using BCrypt
3. JWT token generated with user claims (id, email, role)
4. Client stores token and sends in `Authorization: Bearer <token>` header

### Authorization Layers
1. **Filter Level**: JWT validation and authentication
2. **Config Level**: Role-based endpoint access (@PreAuthorize)
3. **Service Level**: Fine-grained business rules (can this user approve THIS request?)

### Roles & Permissions

| Role | Permissions |
|------|-------------|
| EMPLOYEE | Create requests, view own requests |
| MANAGER | All EMPLOYEE permissions + approve/reject team requests |
| ADMIN | All MANAGER permissions + user management, view all requests |

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.9+
- Docker & Docker Compose (recommended)
- MySQL 8.0 (if running without Docker)

<!--
### Option 1: Run with Docker (Recommended)

```bash
# Clone the repository
git clone <repository-url>
cd approval-system

# Build and run the entire stack (app + database)
docker-compose up --build

# The application will be available at:
# - API: http://localhost:8080
# - Swagger UI: http://localhost:8080/swagger-ui.html
# - MySQL: localhost:3307
```
-->

### Option 1: Run Locally

```bash
# 1. Start MySQL
docker run -d \
  --name mysql-approval \
  -e MYSQL_ROOT_PASSWORD=rootpassword \
  -e MYSQL_DATABASE=approval_system_dev \
  -e MYSQL_USER=approval_user \
  -e MYSQL_PASSWORD=approval_pass \
  -p 3306:3306 \
  mysql:8.0

# 2. Build the application
mvn clean package -DskipTests

# 3. Run the application
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or run the JAR directly
java -jar target/approval-system-1.0.0.jar --spring.profiles.active=dev
```
<img width="677" height="485" alt="Screenshot 2026-07-03 015039" src="https://github.com/user-attachments/assets/3b24a6d8-005a-42a8-b1e0-095652978e9c" />

---

## 📡 API Documentation

### Interactive Documentation
Once the application is running, access Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

<img width="1919" height="965" alt="Screenshot 2026-07-03 015021" src="https://github.com/user-attachments/assets/d749909e-ccd6-41fd-9523-1181f2ebceb7" />


### Key Endpoints

#### Authentication
```http
POST /api/v1/auth/login
POST /api/v1/auth/register
```

#### Users
```http
GET  /api/v1/users/me              # Get current user
GET  /api/v1/users                 # Get all users (Admin)
GET  /api/v1/users/{id}            # Get user by ID
GET  /api/v1/users/subordinates    # Get team members (Manager)
```

#### Requests
```http
POST /api/v1/requests                    # Create request
GET  /api/v1/requests/my-requests        # Get own requests
GET  /api/v1/requests/pending            # Get pending approvals (Manager)
GET  /api/v1/requests/{id}               # Get request details
PUT  /api/v1/requests/{id}/approve       # Approve request
PUT  /api/v1/requests/{id}/reject        # Reject request
```

### Sample Request/Response

**Create Leave Request:**
```json
POST /api/v1/requests
Authorization: Bearer <token>

{
  "requestType": "LEAVE",
  "title": "Annual Vacation",
  "description": "Family trip to Hawaii",
  "startDate": "2026-02-01",
  "endDate": "2026-02-15"
}

Response (201 Created):
{
  "id": 1,
  "requestType": "LEAVE",
  "title": "Annual Vacation",
  "status": "PENDING",
  "createdById": 2,
  "createdByName": "John Doe",
  "approverId": 1,
  "approverName": "Jane Manager",
  "startDate": "2026-02-01",
  "endDate": "2026-02-15",
  "createdAt": "2026-01-09T10:30:00"
}
```

---
<!--
## 🧪 Testing

### Run Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=RequestServiceTest

# Run with coverage
mvn clean test jacoco:report
```

### Test Strategy
- **Unit Tests**: Service layer business logic
- **Integration Tests**: Controller endpoints with MockMvc
- **Focus**: Critical business rules, not 100% coverage

---

## 📈 Monitoring & Health Checks

### Actuator Endpoints
```http
GET /actuator/health      # Health status
GET /actuator/info        # Application info
GET /actuator/metrics     # System metrics
```

### Health Check Response
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

---

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILE` | Active profile (dev/prod) | dev |
| `DB_URL` | Database connection URL | jdbc:mysql://localhost:3306/... |
| `DB_USERNAME` | Database username | approval_user |
| `DB_PASSWORD` | Database password | approval_pass |
| `JWT_SECRET` | JWT signing secret (256-bit min) | changeme |

### Profile-Specific Configuration
- **dev** (`application-dev.yml`): Verbose logging, show SQL, relaxed error handling
- **prod** (`application-prod.yml`): Minimal logging, strict error handling, optimized connections

---
-->

## 🏛️ Project Structure

```
src/main/java/com/company/approval_system/
├── config/              # Security, Swagger, CORS configuration
├── controller/          # REST endpoints
├── dto/                 # Request/Response data transfer objects
│   ├── request/
│   └── response/
├── entity/              # JPA entities
├── enums/               # Role, Status, Type enumerations
├── exception/           # Custom exceptions and global handler
├── repository/          # Spring Data JPA repositories
├── security/            # JWT, UserDetails, Authentication filter
└── service/             # Business logic layer
```

---

## 🎯 Business Logic & Workflows

### Request Lifecycle
```
1. Employee creates request
   ↓
2. System assigns manager as approver
   ↓
3. Status: PENDING
   ↓
4. Manager views pending requests
   ↓
5. Manager approves or rejects
   ↓
6. Status: APPROVED or REJECTED
   ↓
7. Audit entry created in approval_history
```

### Validation Rules
- Leave/Travel requests require start and end dates
- Expense requests require an amount
- End date cannot be before start date
- Only assigned approver can approve/reject
- Cannot approve/reject already processed requests
- Employees must have a manager assigned to create requests

---
<!--
## 🚢 Deployment

### Docker Production Deployment
```bash
# Build image
docker build -t approval-system:1.0.0 .

# Run with production profile
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILE=prod \
  -e DB_URL=jdbc:mysql://prod-db:3306/approval_system \
  -e DB_USERNAME=prod_user \
  -e DB_PASSWORD=<secure-password> \
  -e JWT_SECRET=<256-bit-secret> \
  approval-system:1.0.0
```

### Database Migration
```bash
# First-time setup (creates tables automatically with ddl-auto=update in dev)
# For production, use Flyway or Liquibase for versioned migrations

# Manual schema creation (if needed)
mysql -u root -p < src/main/resources/schema.sql
```

---
-->
## 📋 Development Timeline

### Phase 1: Foundation
- ✅ Database schema design
- ✅ Entity models
- ✅ Repository layer
- ✅ Basic service layer

### Phase 2: Security
- ✅ JWT implementation
- ✅ Authentication endpoints
- ✅ Authorization rules
- ✅ Security configuration

### Phase 3: Business Logic
- ✅ Request creation
- ✅ Approval workflow
- ✅ Validation rules
- ✅ Error handling

### Phase 4: API & Documentation
- ✅ REST controllers
- ✅ DTO mappings
- ✅ Swagger documentation
- ✅ API testing

### Phase 5: Production Readiness
- ✅ Docker containerization
- ✅ Logging configuration
- ✅ Health checks
- ✅ Testing

---
<!--
## 📖 Adoption Strategy

### Internal Rollout Plan

**Phase 1: Pilot**
- Deploy to single department (e.g., Engineering)
- 10-20 users
- Gather feedback

**Phase 2: Expansion (Week 3-4)**
- Roll out to additional departments
- 50-100 users
- Monitor performance

**Phase 3: Company-Wide (Week 5+)**
- All employees
- Full training and documentation
- Support team in place

### Change Management
1. **Communication**: Announce new system via email/Slack
2. **Training**: 30-minute demo session for each department
3. **Support**: Dedicated Slack channel for questions
4. **Incentives**: Highlight time savings and transparency benefits

### Success Metrics
- 70% reduction in approval cycle time
- 95% user adoption within 2 months
- 50% reduction in email volume for approvals
- Zero data loss or security incidents

---

## 🛠️ Maintenance & Support

### Logging
- **Location**: `/var/log/approval-system/` (production)
- **Rotation**: 30 days retention, 10MB per file
- **Levels**: ERROR (always), INFO (business events), DEBUG (dev only)

### Backup Strategy
- **Database**: Daily automated backups
- **Retention**: 30 days
- **Recovery**: Tested monthly

### Performance Optimization
- **Connection Pooling**: HikariCP with 20 max connections
- **Indexes**: Optimized for common queries (status, approver_id)
- **Lazy Loading**: Used for entity relationships
- **Pagination**: All list endpoints support pagination

---
-->
## 🤝 Contributing

### Code Style
- Follow standard Spring Boot conventions
- Use meaningful variable/method names
- Write JavaDoc for public methods
- Keep methods small and focused

### Commit Messages
```
feat: Add expense request validation
fix: Resolve JWT token expiration bug
docs: Update API documentation
refactor: Extract approval logic to service
```

## 📞 Support

### Troubleshooting

**Issue**: Application won't start
```bash
# Check if MySQL is running
docker ps | grep mysql

# Check logs
docker logs approval-app

# Verify database connection
mysql -h localhost -P 3307 -u approval_user -p
```

**Issue**: 401 Unauthorized
- Verify JWT token in Authorization header
- Check token expiration (24 hours by default)
- Confirm user is active in database

**Issue**: 403 Forbidden
- Verify user has required role
- Check endpoint authorization rules in SecurityConfig

---

## ✨ Future Enhancements

- [ ] Multi-level approval workflows
- [ ] Email notifications for pending approvals
- [ ] Mobile push notifications
- [ ] Advanced analytics dashboard
- [ ] File attachment support
- [ ] Integration with HR systems
- [ ] Approval delegation feature
- [ ] Auto-approval based on rules

---
