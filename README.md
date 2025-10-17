# Incident Response Coordinator

A production-ready REST API for incident response workflow management, featuring automated escalation, state machine coordination, and complete audit logging. Built with Spring Boot, containerized with Docker, deployable to Kubernetes, and AWS-ready.

## Project Overview

This Spring Boot application simulates an incident response system similar to those used in cybersecurity operations centers. It manages the complete lifecycle of security and operational incidents from detection through resolution, with built-in SLA monitoring and automated priority escalation.

## Key Features

- **State Machine Workflow** - Enforces valid incident lifecycle transitions (REPORTED → ACKNOWLEDGED → INVESTIGATING → RESOLVED)
- **Automated Priority Escalation** - Background job escalates priority for unacknowledged incidents every 60 seconds
- **Complete Audit Trail** - Every action logged with timestamp and performer for compliance
- **Input Validation** - Request validation with detailed error messages
- **REST API Documentation** - Interactive Swagger UI for testing endpoints
- **In-Memory Database** - H2 database with console for data inspection
- **Docker Support** - Multi-stage containerization with security best practices
- **Kubernetes Ready** - Production-grade K8s manifests included
- **AWS Deployment** - Complete guides for ECS Fargate deployment
- **Professional Architecture** - Clean separation: Controller → Service → Repository → Database

## Tech Stack

- **Java 17** - LTS version
- **Spring Boot 3.3.5** - Application framework (LTS)
- **Spring Data JPA** - Database abstraction
- **Hibernate** - ORM
- **H2 Database** - In-memory database (PostgreSQL for production)
- **Lombok** - Reduce boilerplate code
- **Springdoc OpenAPI** - API documentation (Swagger UI)
- **Docker** - Containerization
- **Kubernetes** - Container orchestration
- **AWS** - Cloud deployment (ECS, EKS, RDS)
- **Maven** - Build tool

## Prerequisites

Before running this project, ensure you have:

- **Java 17 or higher** installed ([Download](https://adoptium.net/))
- **Maven** (included via Maven Wrapper)
- **Git** (to clone the repository)
- **Docker** (optional, for containerization)
- **kubectl** (optional, for Kubernetes deployment)
- **AWS CLI** (optional, for AWS deployment)

### Verify Java Installation
```bash
java -version
# Should show: java version "17.0.x" or higher
```

## Quick Start

### Option 1: Run Locally with Maven
```bash
# 1. Clone the repository
git clone https://github.com/jpak0/incident-response-coordinator.git
cd incident-response-coordinator

# 2. Run the application
./mvnw spring-boot:run

# 3. Wait for startup message:
# "Started IncidentCoordinatorApplication in X.XXX seconds"
# "Tomcat started on port 8080"
```

### Option 2: Run with Docker
```bash
# 1. Build Docker image
docker build -t incident-coordinator:1.0.0 .

# 2. Run container
docker run -d \
  --name incident-coordinator \
  -p 8080:8080 \
  -e SPRING_H2_CONSOLE_SETTINGS_WEB_ALLOW_OTHERS=true \
  incident-coordinator:1.0.0

# 3. View logs
docker logs -f incident-coordinator
```

### Option 3: Deploy to Kubernetes
```bash
# Deploy all resources
kubectl apply -f k8s/all-in-one.yaml

# Check pod status
kubectl get pods -n incident-response

# Get service URL
kubectl get service -n incident-response
```

## Accessing the Application

Once running, you can access:

- **Swagger UI (API Testing):** http://localhost:8080/swagger-ui.html
- **H2 Database Console:** http://localhost:8080/h2-console
- **API Endpoints:** http://localhost:8080/api/incidents

## Complete Incident Workflow Example

This section demonstrates a complete incident lifecycle using the actual API responses from the system.

### Step 1: Create an Incident

**POST** `/api/incidents`

**Request:**
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
  "description": "API response times exceeding 5 seconds. Customer complaints increasing.",
  "severity": "HIGH",
  "state": "REPORTED",
  "priority": "HIGH",
  "reportedAt": "2025-10-16T14:43:49.346743",
  "acknowledgedAt": null,
  "resolvedAt": null,
  "reportedBy": "monitoring-system",
  "assignedTo": null,
  "affectedSystemsCount": 3,
  "commentCount": 0
}
```

**Note:** Copy the `id` field for use in subsequent steps.

---

### Step 2: Acknowledge the Incident

**PUT** `/api/incidents/{id}/acknowledge`

**Request:**
```json
{
  "responder": "sarah.chen@bae.com"
}
```

**Response (200 OK):**
```json
{
  "id": "c06c6684-e235-4d68-b550-f29ef5ee04a0",
  "title": "API Gateway High Latency",
  "description": "API response times exceeding 5 seconds. Customer complaints increasing.",
  "severity": "HIGH",
  "state": "ACKNOWLEDGED",
  "priority": "HIGH",
  "reportedAt": "2025-10-16T14:43:49.346743",
  "acknowledgedAt": "2025-10-16T14:44:49.2994",
  "resolvedAt": null,
  "reportedBy": "monitoring-system",
  "assignedTo": "sarah.chen@bae.com",
  "affectedSystemsCount": 3,
  "commentCount": 0
}
```

**Changes:**
- `state` changed from `REPORTED` to `ACKNOWLEDGED`
- `assignedTo` now set to responder
- `acknowledgedAt` timestamp recorded

---

### Step 3: Start Investigation

**PUT** `/api/incidents/{id}/investigate`

**Request:**
```json
{
  "responder": "sarah.chen@bae.com"
}
```

**Response (200 OK):**
```json
{
  "id": "c06c6684-e235-4d68-b550-f29ef5ee04a0",
  "title": "API Gateway High Latency",
  "description": "API response times exceeding 5 seconds. Customer complaints increasing.",
  "severity": "HIGH",
  "state": "INVESTIGATING",
  "priority": "HIGH",
  "reportedAt": "2025-10-16T14:43:49.346743",
  "acknowledgedAt": "2025-10-16T14:44:49.2994",
  "resolvedAt": null,
  "reportedBy": "monitoring-system",
  "assignedTo": "sarah.chen@bae.com",
  "affectedSystemsCount": 3,
  "commentCount": 0
}
```

**Changes:**
- `state` changed from `ACKNOWLEDGED` to `INVESTIGATING`

---

### Step 4: Add a Comment

**POST** `/api/incidents/{id}/comments`

**Request:**
```json
{
  "author": "sarah.chen@bae.com",
  "content": "Scaling API gateway instances from 3 to 6. Monitoring response times."
}
```

**Response (200 OK):**
```json
{
  "id": "c06c6684-e235-4d68-b550-f29ef5ee04a0",
  "title": "API Gateway High Latency",
  "description": "API response times exceeding 5 seconds. Customer complaints increasing.",
  "severity": "HIGH",
  "state": "INVESTIGATING",
  "priority": "HIGH",
  "reportedAt": "2025-10-16T14:43:49.346743",
  "acknowledgedAt": "2025-10-16T14:44:49.2994",
  "resolvedAt": null,
  "reportedBy": "monitoring-system",
  "assignedTo": "sarah.chen@bae.com",
  "affectedSystemsCount": 3,
  "commentCount": 1
}
```

**Changes:**
- `commentCount` increased to 1

---

### Step 5: Resolve the Incident

**PUT** `/api/incidents/{id}/resolve`

**Request:**
```json
{
  "responder": "sarah.chen@bae.com"
}
```

**Response (200 OK):**
```json
{
  "id": "c06c6684-e235-4d68-b550-f29ef5ee04a0",
  "title": "API Gateway High Latency",
  "description": "API response times exceeding 5 seconds. Customer complaints increasing.",
  "severity": "HIGH",
  "state": "RESOLVED",
  "priority": "HIGH",
  "reportedAt": "2025-10-16T14:43:49.346743",
  "acknowledgedAt": "2025-10-16T14:44:49.2994",
  "resolvedAt": "2025-10-16T14:47:52.48203",
  "reportedBy": "monitoring-system",
  "assignedTo": "sarah.chen@bae.com",
  "affectedSystemsCount": 3,
  "commentCount": 1
}
```

**Changes:**
- `state` changed from `INVESTIGATING` to `RESOLVED`
- `resolvedAt` timestamp recorded

---

## Database Inspection

### Accessing H2 Console

1. Navigate to: http://localhost:8080/h2-console
2. Enter connection details:
   - **JDBC URL:** `jdbc:h2:mem:incidentdb`
   - **Username:** `sa`
   - **Password:** (leave blank)
3. Click **Connect**

### Sample Query Results

**Query 1: View All Incidents**
```sql
SELECT * FROM INCIDENTS;
```

**Result:**
```
AFFECTED_SYSTEMS_COUNT | ACKNOWLEDGED_AT          | CLOSED_AT | REPORTED_AT
-----------------------|--------------------------|-----------|---------------------------
5                      | 2025-10-16 14:33:02.031  | null      | 2025-10-16 14:29:22.294
3                      | 2025-10-16 14:44:49.299  | null      | 2025-10-16 14:43:49.346
```

---

**Query 2: View Complete Audit Trail**
```sql
SELECT 
    action, 
    performed_by, 
    details, 
    timestamp 
FROM ACTIVITY_LOGS 
ORDER BY timestamp DESC;
```

**Result:**
```
ACTION                | PERFORMED_BY          | DETAILS                           | TIMESTAMP
----------------------|-----------------------|-----------------------------------|---------------------------
RESOLVED              | sarah.chen@bae.com    | Incident resolved                 | 2025-10-16 14:47:52.482
COMMENT_ADDED         | sarah.chen@bae.com    | Added comment                     | 2025-10-16 14:46:44.026
INVESTIGATION_STARTED | sarah.chen@bae.com    | Investigation started             | 2025-10-16 14:46:02.655
ACKNOWLEDGED          | sarah.chen@bae.com    | Incident acknowledged and assigned| 2025-10-16 14:44:49.300
ACKNOWLEDGED          | string                | Incident acknowledged and assigned| 2025-10-16 14:33:02.031
```

**Complete audit trail showing all actions performed on incidents.**

---

**Query 3: Count Incidents by State**
```sql
SELECT state, COUNT(*) as count 
FROM INCIDENTS 
GROUP BY state;
```

**Result:**
```
STATE         | COUNT
--------------|------
ACKNOWLEDGED  | 1
RESOLVED      | 1
```

---

**Query 4: View All Comments**
```sql
SELECT * FROM COMMENTS;
```

**Result:**
```
IS_SYSTEM_GENERATED | TIMESTAMP                 | ID                       | INCIDENT_ID
--------------------|---------------------------|--------------------------|---------------------------
FALSE               | 2025-10-16 14:46:44.025   | 1e1f80fd-9c2a-441d-b18b  | c06c6684-e235-4d68-b550
```

Shows the comment added during the workflow with complete metadata.

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

## Using cURL
```bash
# Create incident
curl -X POST http://localhost:8080/api/incidents \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Production Database Connection Timeout",
    "severity": "CRITICAL",
    "reportedBy": "monitoring-system",
    "affectedSystemsCount": 5
  }'

# Get all incidents
curl http://localhost:8080/api/incidents

# Acknowledge incident (replace {id} with actual UUID)
curl -X PUT http://localhost:8080/api/incidents/{id}/acknowledge \
  -H "Content-Type: application/json" \
  -d '{"responder": "john.doe@bae.com"}'
```

## Docker Deployment

### Build Image
```bash
docker build -t incident-coordinator:1.0.0 .
```

### Run Container
```bash
docker run -d \
  --name incident-coordinator \
  -p 8080:8080 \
  -e SPRING_H2_CONSOLE_SETTINGS_WEB_ALLOW_OTHERS=true \
  incident-coordinator:1.0.0
```

### View Logs
```bash
docker logs -f incident-coordinator
```

### Stop and Remove
```bash
docker stop incident-coordinator
docker rm incident-coordinator
```

### Docker Image Details

- **Base Image:** Eclipse Temurin 17 JRE
- **Size:** ~500MB
- **Build Type:** Multi-stage (builder + runtime)
- **Security:** Runs as non-root user
- **Health Check:** Enabled on `/api/incidents` endpoint

## Kubernetes Deployment

### Quick Deploy
```bash
# Deploy all resources
kubectl apply -f k8s/all-in-one.yaml

# Wait for pods to be ready
kubectl get pods -n incident-response -w

# Get service external IP (for cloud providers)
kubectl get service -n incident-response
```

### Architecture

- **Namespace:** `incident-response` (resource isolation)
- **Replicas:** 2 pods (high availability)
- **Service Type:** LoadBalancer (external access)
- **Health Checks:** Liveness and readiness probes
- **Resources:** 512Mi-1Gi memory, 250m-500m CPU per pod

### Scaling
```bash
# Scale to 3 replicas
kubectl scale deployment incident-coordinator --replicas=3 -n incident-response

# Verify
kubectl get pods -n incident-response
```

### Access Application

**Cloud providers (AWS EKS, Azure AKS, Google GKE):**
```bash
kubectl get service incident-coordinator-service -n incident-response
# Access at: http://<EXTERNAL-IP>/swagger-ui.html
```

**Local (minikube):**
```bash
minikube service incident-coordinator-service -n incident-response --url
```

### Detailed Documentation

See [k8s/README.md](k8s/README.md) for complete deployment instructions, troubleshooting, and production considerations.

## AWS Deployment

Deploy to Amazon Web Services with multiple options.

### Recommended: ECS Fargate

Serverless container deployment with auto-scaling.

**Estimated Cost:** $80-350/month depending on scale

### Architecture
```
Internet → ALB (Load Balancer) → ECS Fargate Tasks (2+) → RDS PostgreSQL
                                        ↓
                                  CloudWatch Logs
```

### Deployment Options

- **[ECS Fargate](docs/aws/ecs-fargate.md)** - Serverless containers (recommended)
- **[EKS Deployment](docs/aws/README.md)** - Managed Kubernetes
- **[Cost Estimates](docs/aws/cost-estimates.md)** - Detailed pricing breakdown

### Features

- Auto-scaling (2-10 tasks)
- High availability (Multi-AZ)
- Load balancing
- CloudWatch monitoring
- Automated deployments

See [AWS Deployment Guide](docs/aws/README.md) for complete instructions.

## Project Structure
```
src/main/java/com/incidentresponse/
├── IncidentCoordinatorApplication.java    # Main application entry point
├── controller/                            # REST endpoints
│   └── IncidentController.java
├── service/                               # Business logic
│   └── IncidentService.java
├── repository/                            # Data access layer
│   ├── IncidentRepository.java
│   ├── ResponderRepository.java
│   ├── CommentRepository.java
│   └── ActivityLogRepository.java
├── model/                                 # JPA entities
│   ├── Incident.java
│   ├── Responder.java
│   ├── Comment.java
│   ├── ActivityLog.java
│   ├── Severity.java
│   ├── Priority.java
│   └── IncidentState.java
├── dto/                                   # API request/response objects
│   ├── CreateIncidentRequest.java
│   ├── IncidentResponse.java
│   ├── AcknowledgeRequest.java
│   └── AddCommentRequest.java
└── exception/                             # Error handling
    ├── GlobalExceptionHandler.java
    └── IncidentNotFoundException.java
```

## Incident Lifecycle States
```
REPORTED → ACKNOWLEDGED → INVESTIGATING → MITIGATING → RESOLVED → CLOSED
```

### State Transition Rules

- Can only acknowledge incidents in `REPORTED` state
- Can only start investigation after `ACKNOWLEDGED`
- Can resolve from `INVESTIGATING` or `MITIGATING`
- Invalid transitions return 400 Bad Request with error message

**Example Error:**
```json
{
  "status": 400,
  "message": "Can only acknowledge incidents in REPORTED state",
  "timestamp": "2025-10-16T14:33:23.504058"
}
```

## Configuration

Key settings in `src/main/resources/application.properties`:
```properties
# Server
server.port=8080

# Database (H2 in-memory for development)
spring.datasource.url=jdbc:h2:mem:incidentdb

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

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
3. Check the incident - priority will have escalated
4. View audit trail to see escalation log entry

## Stopping the Application

**Maven:**
```bash
# Press Ctrl + C in the terminal
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
# Clean and recompile
./mvnw clean compile

# Clean and run tests
./mvnw clean test

# Package as JAR
./mvnw clean package
```

## Development vs Production

| Feature | Development (H2) | Production (AWS) |
|---------|------------------|------------------|
| Database | H2 in-memory | RDS PostgreSQL |
| Persistence | No (resets on restart) | Yes (durable storage) |
| Scaling | Single instance | Auto-scaling 2-10 instances |
| Availability | Single point of failure | Multi-AZ, load balanced |
| Cost | Free | $80-350/month |
| Monitoring | Console logs | CloudWatch Logs & Metrics |

## License

This project is created for educational and portfolio purposes.

## Author

**Joseph Pak**
- GitHub: [@jpak0](https://github.com/jpak0)
- Project Link: [https://github.com/jpak0/incident-response-coordinator](https://github.com/jpak0/incident-response-coordinator)

## Acknowledgments

Built as a portfolio project demonstrating:
- RESTful API design
- State machine patterns
- Clean architecture principles
- Enterprise Java development practices
- Docker containerization
- Kubernetes orchestration
- AWS cloud deployment
- Suitable for cybersecurity operations and incident management systems