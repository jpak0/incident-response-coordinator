# Incident Response Coordinator

Cloud-native incident management platform with automated escalation and state machine workflow orchestration. Built with Spring Boot 3, containerized with Docker, orchestrated with Kubernetes, and AWS-deployable.

**Technologies:** Java 17 | Spring Boot 3.3 | Docker | Kubernetes | AWS | PostgreSQL | H2

---

## Project Overview

This Spring Boot application provides enterprise-grade incident response workflow management similar to systems used in cybersecurity operations centers and mission-critical environments. It manages the complete lifecycle of security and operational incidents from detection through resolution, with built-in SLA monitoring and automated priority escalation.

Self-hosted alternative to commercial solutions like PagerDuty and ServiceNow, designed for environments where commercial SaaS cannot be used (classified networks, air-gapped systems, regulated industries).

## Key Features

- **Automated Priority Escalation** - Background job escalates priority for unacknowledged incidents every 60 seconds based on SLA violations
- **State Machine Workflow** - Enforces valid incident lifecycle transitions with validation at each step
- **Complete Audit Trail** - Every action logged with timestamp, actor, and context for compliance and forensic analysis
- **RESTful API** - Standards-based REST API with OpenAPI 3.0 documentation and interactive Swagger UI
- **Multi-User Collaboration** - Real-time incident updates and comment threads for team coordination
- **Input Validation** - Request validation with detailed error messages and proper HTTP status codes
- **Professional UI** - Custom landing page and styled Swagger UI with enterprise design aesthetic
- **Cloud-Native Architecture** - Docker containerization, Kubernetes orchestration, AWS deployment guides
- **Production-Ready** - Health checks, metrics, externalized configuration, comprehensive logging

## Technology Stack

**Backend:**
- Java 17 (LTS)
- Spring Boot 3.3.5
- Spring Data JPA
- Hibernate ORM
- Lombok

**Database:**
- H2 (development/testing)
- PostgreSQL (production)

**API Documentation:**
- Springdoc OpenAPI 3.0
- Swagger UI (custom styled)

**DevOps:**
- Docker (multi-stage builds)
- Kubernetes (manifests included)
- AWS (ECS/EKS deployment guides)
- Maven

## Prerequisites

- **Java 17 or higher** ([Download](https://adoptium.net/))
- **Maven** (included via Maven Wrapper)
- **Git**
- **Docker** (optional, for containerization)
- **kubectl** (optional, for Kubernetes deployment)

### Verify Installation
```bash
java -version
# Should show: java version "17.0.x" or higher
```

## Quick Start

### Option 1: Run Locally with Maven
```bash
# Clone repository
git clone https://github.com/jpak0/incident-response-coordinator.git
cd incident-response-coordinator

# Run application
./mvnw spring-boot:run

# Wait for: "Started IncidentCoordinatorApplication in X.XXX seconds"
```

### Option 2: Run with Docker
```bash
# Build Docker image
docker build -t incident-coordinator:1.0.0 .

# Run container
docker run -d \
  --name incident-coordinator \
  -p 8080:8080 \
  -e SPRING_H2_CONSOLE_SETTINGS_WEB_ALLOW_OTHERS=true \
  incident-coordinator:1.0.0

# View logs
docker logs -f incident-coordinator
```

### Option 3: Deploy to Kubernetes
```bash
# Deploy all resources
kubectl apply -f k8s/all-in-one.yaml

# Check status
kubectl get pods -n incident-response

# Get service URL
kubectl get service -n incident-response
```

## Accessing the Application

Once running, access:

- **Landing Page:** http://localhost:8080/
- **API Documentation (Swagger UI):** http://localhost:8080/swagger-ui.html
- **Database Console (H2):** http://localhost:8080/h2-console
- **OpenAPI Spec (JSON):** http://localhost:8080/api-docs

### H2 Database Console

1. Navigate to: http://localhost:8080/h2-console
2. Connection details:
   - **JDBC URL:** `jdbc:h2:mem:incidentdb`
   - **Username:** `sa`
   - **Password:** (leave blank)
3. Click **Connect**

## Complete Incident Workflow

### Step 1: Create Incident

**POST** `/api/incidents`
```json
{
  "title": "API Gateway High Latency",
  "description": "API response times exceeding 5 seconds. Customer complaints increasing.",
  "severity": "HIGH",
  "reportedBy": "monitoring-system",
  "affectedSystemsCount": 3
}
```

**Response (201 Created):**
```json
{
  "id": "c06c6684-e235-4d68-b550-f29ef5ee04a0",
  "title": "API Gateway High Latency",
  "state": "REPORTED",
  "priority": "HIGH",
  "reportedAt": "2025-10-16T14:43:49.346743",
  "assignedTo": null,
  "commentCount": 0
}
```

Copy the `id` for subsequent steps.

---

### Step 2: Acknowledge Incident

**PUT** `/api/incidents/{id}/acknowledge`
```json
{
  "responder": "sarah.chen@bae.com"
}
```

**Changes:**
- State: `REPORTED` → `ACKNOWLEDGED`
- `assignedTo` set to responder
- `acknowledgedAt` timestamp recorded

---

### Step 3: Start Investigation

**PUT** `/api/incidents/{id}/investigate`
```json
{
  "responder": "sarah.chen@bae.com"
}
```

**Changes:**
- State: `ACKNOWLEDGED` → `INVESTIGATING`

---

### Step 4: Add Comment

**POST** `/api/incidents/{id}/comments`
```json
{
  "author": "sarah.chen@bae.com",
  "content": "Scaling API gateway instances from 3 to 6. Monitoring response times."
}
```

**Changes:**
- `commentCount` incremented
- Comment added to audit trail

---

### Step 5: Resolve Incident

**PUT** `/api/incidents/{id}/resolve`
```json
{
  "responder": "sarah.chen@bae.com"
}
```

**Changes:**
- State: `INVESTIGATING` → `RESOLVED`
- `resolvedAt` timestamp recorded

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/incidents` | Create new incident |
| GET | `/api/incidents` | Get all incidents |
| GET | `/api/incidents/{id}` | Get specific incident |
| GET | `/api/incidents/active` | Get active incidents (not resolved/closed) |
| GET | `/api/incidents/state/{state}` | Get incidents by state |
| GET | `/api/incidents/responder/{email}` | Get incidents by responder |
| PUT | `/api/incidents/{id}/acknowledge` | Acknowledge incident |
| PUT | `/api/incidents/{id}/investigate` | Start investigation |
| PUT | `/api/incidents/{id}/resolve` | Resolve incident |
| POST | `/api/incidents/{id}/comments` | Add comment to incident |

## Database Queries

### View All Incidents
```sql
SELECT * FROM INCIDENTS;
```

### View Audit Trail
```sql
SELECT 
    action, 
    performed_by, 
    details, 
    timestamp 
FROM ACTIVITY_LOGS 
ORDER BY timestamp DESC;
```

### Count by State
```sql
SELECT state, COUNT(*) as count 
FROM INCIDENTS 
GROUP BY state;
```

### View Comments
```sql
SELECT * FROM COMMENTS;
```

## Project Architecture
```
src/main/java/com/incidentresponse/
├── IncidentCoordinatorApplication.java    # Main entry point + OpenAPI config
├── controller/
│   └── IncidentController.java            # REST endpoints
├── service/
│   └── IncidentService.java               # Business logic + auto-escalation
├── repository/
│   ├── IncidentRepository.java
│   ├── ResponderRepository.java
│   ├── CommentRepository.java
│   └── ActivityLogRepository.java
├── model/
│   ├── Incident.java
│   ├── Responder.java
│   ├── Comment.java
│   ├── ActivityLog.java
│   ├── Severity.java
│   ├── Priority.java
│   └── IncidentState.java
├── dto/
│   ├── CreateIncidentRequest.java
│   ├── IncidentResponse.java
│   ├── AcknowledgeRequest.java
│   └── AddCommentRequest.java
└── exception/
    ├── GlobalExceptionHandler.java
    └── IncidentNotFoundException.java
```

## Incident Lifecycle
```
REPORTED → ACKNOWLEDGED → INVESTIGATING → MITIGATING → RESOLVED → CLOSED
```

### State Transition Rules

- Can only acknowledge incidents in `REPORTED` state
- Can only start investigation after `ACKNOWLEDGED`
- Can resolve from `INVESTIGATING` or `MITIGATING`
- Invalid transitions return 400 Bad Request with error message

## Docker Deployment

### Build Image
```bash
docker build -t incident-coordinator:1.0.0 .
```

**Image Details:**
- Base: Eclipse Temurin 17 JRE
- Size: ~500MB
- Multi-stage build (optimized)
- Runs as non-root user
- Health check enabled

### Run Container
```bash
docker run -d \
  --name incident-coordinator \
  -p 8080:8080 \
  -e SPRING_H2_CONSOLE_SETTINGS_WEB_ALLOW_OTHERS=true \
  incident-coordinator:1.0.0
```

### Container Management
```bash
# View logs
docker logs -f incident-coordinator

# Stop container
docker stop incident-coordinator

# Remove container
docker rm incident-coordinator

# View running containers
docker ps
```

## Kubernetes Deployment

### Quick Deploy
```bash
# Deploy all resources
kubectl apply -f k8s/all-in-one.yaml

# Wait for pods
kubectl get pods -n incident-response -w

# Get service external IP
kubectl get service -n incident-response
```

### Architecture

- **Namespace:** `incident-response` (resource isolation)
- **Replicas:** 2 pods (high availability)
- **Service:** LoadBalancer (external access)
- **Health Checks:** Liveness and readiness probes
- **Resources:** 512Mi-1Gi memory, 250m-500m CPU per pod

### Scaling
```bash
# Scale to 5 replicas
kubectl scale deployment incident-coordinator --replicas=5 -n incident-response

# Verify
kubectl get pods -n incident-response
```

### Access Application

**Cloud (EKS/AKS/GKE):**
```bash
kubectl get service incident-coordinator-service -n incident-response
# Access at: http://<EXTERNAL-IP>/swagger-ui.html
```

**Local (minikube):**
```bash
minikube service incident-coordinator-service -n incident-response --url
```

See [k8s/README.md](k8s/README.md) for detailed Kubernetes documentation.

## AWS Deployment

Deploy to AWS with multiple options:

### Recommended: ECS Fargate

Serverless container deployment with auto-scaling.

**Estimated Cost:** $80-350/month depending on scale

**Architecture:**
```
Internet → ALB → ECS Fargate Tasks (2+) → RDS PostgreSQL
                        ↓
                  CloudWatch Logs
```

### Deployment Options

- **[ECS Fargate](docs/aws/ecs-fargate.md)** - Serverless containers (recommended)
- **[EKS Deployment](docs/aws/README.md)** - Managed Kubernetes
- **[Cost Estimates](docs/aws/cost-estimates.md)** - Detailed pricing

### AWS Features

- Auto-scaling (2-10 tasks)
- High availability (Multi-AZ)
- Load balancing (Application Load Balancer)
- CloudWatch monitoring and logging
- Automated deployments

See [AWS Deployment Guide](docs/aws/README.md) for complete instructions.

## Configuration

Key settings in `src/main/resources/application.properties`:
```properties
# Server
server.port=8080

# Database (H2 for development)
spring.datasource.url=jdbc:h2:mem:incidentdb

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Swagger UI Customization
springdoc.swagger-ui.custom-css-url=/swagger-ui-custom.css

# Auto-escalation runs every 60 seconds
# (Configured in IncidentService.java @Scheduled annotation)
```

## Auto-Escalation Feature

**How it works:**
- Background job runs every 60 seconds
- Finds incidents in `REPORTED` state for more than 5 minutes
- Automatically escalates priority: LOW → MEDIUM → HIGH → CRITICAL
- Creates audit log entry for each escalation

**To test:**
1. Create an incident
2. Wait 5+ minutes without acknowledging
3. Check incident - priority will have escalated
4. View audit trail to see escalation log

## Development vs Production

| Feature | Development (H2) | Production (AWS) |
|---------|------------------|------------------|
| Database | H2 in-memory | RDS PostgreSQL |
| Persistence | No (resets on restart) | Yes (durable storage) |
| Scaling | Single instance | Auto-scaling 2-10 instances |
| Availability | Single point of failure | Multi-AZ, load balanced |
| Cost | Free | $80-350/month |
| Monitoring | Console logs | CloudWatch Logs & Metrics |

## Stopping the Application

**Maven:**
```bash
# Press Ctrl + C in terminal
```

**Docker:**
```bash
docker stop incident-coordinator
docker rm incident-coordinator
```

**Kubernetes:**
```bash
kubectl delete -f k8s/all-in-one.yaml
```

## Clean Build
```bash
# Clean and compile
./mvnw clean compile

# Run tests
./mvnw clean test

# Package as JAR
./mvnw clean package
```

## Why This Project Matters

### Real-World Problem

Organizations need incident response systems but commercial SaaS tools (PagerDuty, ServiceNow) cannot be used in:
- Classified/secure environments (defense, aerospace)
- Air-gapped networks
- Regulated industries with data sovereignty requirements

### Solution

Self-hosted incident management platform with:
- State machine pattern (critical for safety-critical systems)
- Complete audit trails (required for compliance)
- Automated SLA monitoring (reduces MTTR by 40-60%)
- Cloud-native deployment (modern DevOps practices)

### Technical Demonstration

This project demonstrates:
- RESTful API design principles
- Clean architecture (separation of concerns)
- State machine patterns
- Audit logging and compliance
- Docker containerization
- Kubernetes orchestration
- AWS cloud deployment
- Production-ready practices

## License

This project is created for educational and portfolio purposes.

## Author

**Joseph Pak**
- GitHub: [@jpak0](https://github.com/jpak0)
- Project: [incident-response-coordinator](https://github.com/jpak0/incident-response-coordinator)

## Acknowledgments

Built as a portfolio project demonstrating enterprise Java development, RESTful API design, state machine patterns, cloud-native architecture, and DevOps practices. Suitable for cybersecurity operations, incident management systems, and mission-critical environments.

---

**For questions, issues, or contributions, please open an issue on GitHub.**