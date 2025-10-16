# Incident Response System - Usage Examples

Complete guide with code examples for all entities and repositories.

---

## Table of Contents
- [Domain Models (Entities)](#domain-models)
  - [Enums](#enums)
  - [Responder](#responder-entity)
  - [Incident](#incident-entity)
  - [Comment](#comment-entity)
  - [ActivityLog](#activitylog-entity)
- [Repositories](#repositories)
- [Complete Workflow Example](#complete-workflow)

---

## Domain Models

### Enums

#### Severity
Represents incident severity levels:
```java
// Available values
Severity.LOW
Severity.MEDIUM
Severity.HIGH
Severity.CRITICAL

// Usage in incident creation
Incident incident = Incident.builder()
    .severity(Severity.CRITICAL)
    .build();
```

#### Priority
Represents incident urgency (can be auto-escalated):
```java
// Available values
Priority.LOW
Priority.MEDIUM
Priority.HIGH
Priority.CRITICAL

// Usage
incident.setPriority(Priority.HIGH);
```

#### IncidentState
Represents workflow states:
```java
// Available states (in order)
IncidentState.REPORTED       // Initial state
IncidentState.ACKNOWLEDGED   // Assigned to responder
IncidentState.INVESTIGATING  // Active work
IncidentState.MITIGATING     // Implementing fix
IncidentState.RESOLVED       // Fixed
IncidentState.CLOSED         // Archived

// Usage
if (incident.getState() == IncidentState.INVESTIGATING) {
    // Do something
}
```

---

### Responder Entity

Represents personnel who respond to incidents.

#### Creating a Responder
```java
// Basic responder
Responder responder = Responder.builder()
    .name("John Doe")
    .email("john.doe@bae.com")
    .build();
// Defaults: role="RESPONDER", onCall=false

// On-call engineer
Responder oncallEngineer = Responder.builder()
    .name("Sarah Chen")
    .email("sarah.chen@bae.com")
    .role("ENGINEER")
    .onCall(true)
    .build();

// Security analyst
Responder analyst = Responder.builder()
    .name("Mike Rodriguez")
    .email("mike.rodriguez@bae.com")
    .role("SECURITY_ANALYST")
    .onCall(true)
    .build();
```

#### Saving and Querying
```java
// Save responder
responderRepository.save(responder);

// Find by email
Optional<Responder> found = responderRepository.findByEmail("john.doe@bae.com");

// Find all on-call responders
List<Responder> oncall = responderRepository.findByOnCallTrue();

// Find by role
List<Responder> engineers = responderRepository.findByRole("ENGINEER");
```

---

### Incident Entity

Main entity representing a security or operational incident.

#### Creating an Incident
```java
// Critical database incident
Incident incident = Incident.builder()
    .title("Production Database Connection Timeout")
    .description("Database cluster not responding to queries. Multiple services affected.")
    .severity(Severity.CRITICAL)
    .reportedBy("monitoring-system")
    .affectedSystemsCount(5)
    .build();
// Defaults: state=REPORTED, priority=MEDIUM, reportedAt=now()

// Simple incident with minimal info
Incident simpleIncident = Incident.builder()
    .title("Login page slow response")
    .severity(Severity.LOW)
    .reportedBy("john.doe@bae.com")
    .build();
```

#### State Transitions
```java
// 1. REPORTED → ACKNOWLEDGED
incident.acknowledge("sarah.chen@bae.com");
// Now: state=ACKNOWLEDGED, assignedTo="sarah.chen@bae.com", acknowledgedAt=now()

// 2. ACKNOWLEDGED → INVESTIGATING
incident.startInvestigation("sarah.chen@bae.com");
// Now: state=INVESTIGATING

// 3. INVESTIGATING → RESOLVED
incident.resolve("sarah.chen@bae.com");
// Now: state=RESOLVED, resolvedAt=now()

// Each transition creates an ActivityLog entry automatically
```

#### Adding Comments
```java
// Add human comment
incident.addComment("sarah.chen@bae.com", "Identified root cause: connection pool exhaustion");

// Add system comment (done internally by state transitions)
incident.addComment("SYSTEM", "Priority escalated from MEDIUM to HIGH");
```

#### Auto-Escalation
```java
// Manually escalate priority (usually done by scheduled job)
incident.escalate();
// Priority: LOW → MEDIUM → HIGH → CRITICAL
// Creates ActivityLog entry
```

---

### Comment Entity

Represents updates and collaboration on an incident.

#### Creating Comments
```java
// Human comment
Comment userComment = Comment.builder()
    .incident(incident)
    .author("john.doe@bae.com")
    .content("Increased connection pool size from 20 to 50. Monitoring for improvements.")
    .isSystemGenerated(false)
    .build();

// System-generated comment
Comment systemComment = Comment.builder()
    .incident(incident)
    .author("SYSTEM")
    .content("Incident priority escalated from HIGH to CRITICAL due to SLA breach")
    .isSystemGenerated(true)
    .build();

// Save comment
commentRepository.save(userComment);
```

#### Querying Comments
```java
// Get all comments for an incident (ordered by newest first)
List<Comment> comments = commentRepository.findByIncidentIdOrderByTimestampDesc(incidentId);

// Get all comments by a specific author
List<Comment> myComments = commentRepository.findByAuthor("john.doe@bae.com");
```

---

### ActivityLog Entity

Complete audit trail of all actions on an incident.

#### Creating Activity Logs
```java
// Log acknowledgment
ActivityLog acknowledgmentLog = ActivityLog.builder()
    .incident(incident)
    .action("ACKNOWLEDGED")
    .performedBy("sarah.chen@bae.com")
    .details("Incident acknowledged and assigned to security team")
    .build();

// Log escalation (auto-generated by system)
ActivityLog escalationLog = ActivityLog.builder()
    .incident(incident)
    .action("ESCALATED")
    .performedBy("SYSTEM")
    .details("Priority escalated from LOW to MEDIUM (Unacknowledged for 5+ minutes)")
    .build();

// Log resolution
ActivityLog resolutionLog = ActivityLog.builder()
    .incident(incident)
    .action("RESOLVED")
    .performedBy("sarah.chen@bae.com")
    .details("Connection pool configuration fixed. Issue resolved.")
    .build();

// Save log
activityLogRepository.save(acknowledgmentLog);
```

#### Querying Activity Logs
```java
// Get complete audit trail for incident
List<ActivityLog> history = activityLogRepository.findByIncidentIdOrderByTimestampDesc(incidentId);

// Get all actions performed by a user
List<ActivityLog> userActions = activityLogRepository.findByPerformedBy("sarah.chen@bae.com");
```

---

## Repositories

Spring Data JPA repositories provide database access without writing SQL.

### IncidentRepository
```java
// Save incident
incidentRepository.save(incident);

// Find by ID
Optional<Incident> found = incidentRepository.findById(incidentId);

// Find all incidents
List<Incident> all = incidentRepository.findAll();

// Find by state
List<Incident> investigating = incidentRepository.findByState(IncidentState.INVESTIGATING);

// Find by severity
List<Incident> critical = incidentRepository.findBySeverity(Severity.CRITICAL);

// Find by assigned responder
List<Incident> myIncidents = incidentRepository.findByAssignedTo("john.doe@bae.com");

// Find by reporter
List<Incident> reported = incidentRepository.findByReportedBy("monitoring-system");

// Find active incidents (not closed)
List<IncidentState> closedStates = Arrays.asList(IncidentState.CLOSED);
List<Incident> active = incidentRepository.findByStateNotIn(closedStates);

// Find high-priority active incidents
List<Priority> highPriorities = Arrays.asList(Priority.HIGH, Priority.CRITICAL);
List<IncidentState> inactiveStates = Arrays.asList(IncidentState.RESOLVED, IncidentState.CLOSED);
List<Incident> urgent = incidentRepository.findByPriorityInAndStateNotIn(highPriorities, inactiveStates);

// Find incidents needing escalation (custom query)
LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
List<Incident> needsEscalation = incidentRepository.findIncidentsNeedingEscalation(
    IncidentState.REPORTED, 
    fiveMinutesAgo
);
```

### ResponderRepository
```java
// Find by email
Optional<Responder> responder = responderRepository.findByEmail("john.doe@bae.com");

// Find all on-call responders
List<Responder> oncall = responderRepository.findByOnCallTrue();

// Find by role
List<Responder> engineers = responderRepository.findByRole("ENGINEER");
```

### CommentRepository
```java
// Get all comments for incident (ordered newest first)
List<Comment> comments = commentRepository.findByIncidentIdOrderByTimestampDesc(incidentId);

// Get all comments by author
List<Comment> myComments = commentRepository.findByAuthor("john.doe@bae.com");
```

### ActivityLogRepository
```java
// Get audit trail for incident (ordered newest first)
List<ActivityLog> logs = activityLogRepository.findByIncidentIdOrderByTimestampDesc(incidentId);

// Get all actions by user
List<ActivityLog> userActions = activityLogRepository.findByPerformedBy("sarah.chen@bae.com");
```

---

## Complete Workflow Example

End-to-end example of incident lifecycle:
```java
// ========================================
// 1. INCIDENT REPORTED
// ========================================

// Monitoring system detects issue
Incident incident = Incident.builder()
    .title("Production API Gateway Timeout")
    .description("Gateway returning 504 errors. Multiple downstream services affected.")
    .severity(Severity.CRITICAL)
    .reportedBy("monitoring-system")
    .affectedSystemsCount(8)
    .build();

incidentRepository.save(incident);
// State: REPORTED, Priority: MEDIUM (will escalate if not acknowledged)

// ========================================
// 2. FIND ON-CALL RESPONDER
// ========================================

List<Responder> oncall = responderRepository.findByOnCallTrue();
Responder responder = oncall.get(0); // Get first available

// ========================================
// 3. ACKNOWLEDGE INCIDENT
// ========================================

incident.acknowledge(responder.getEmail());
incidentRepository.save(incident);
// State: ACKNOWLEDGED
// ActivityLog: "ACKNOWLEDGED by sarah.chen@bae.com"

// ========================================
// 4. START INVESTIGATION
// ========================================

incident.startInvestigation(responder.getEmail());
incidentRepository.save(incident);
// State: INVESTIGATING
// ActivityLog: "INVESTIGATION_STARTED by sarah.chen@bae.com"

// Add investigation findings
incident.addComment(responder.getEmail(), 
    "Checking gateway logs. High memory usage detected on gateway-03.");
incidentRepository.save(incident);

// ========================================
// 5. MORE INVESTIGATION
// ========================================

incident.addComment(responder.getEmail(),
    "Memory leak confirmed in gateway service. Restarting affected instances.");
incidentRepository.save(incident);

// ========================================
// 6. RESOLVE INCIDENT
// ========================================

incident.resolve(responder.getEmail());
incidentRepository.save(incident);
// State: RESOLVED, resolvedAt: now()
// ActivityLog: "RESOLVED by sarah.chen@bae.com"

incident.addComment(responder.getEmail(),
    "All gateway instances restarted. Memory usage back to normal. Monitoring for 30 minutes.");
incidentRepository.save(incident);

// ========================================
// 7. REVIEW AUDIT TRAIL
// ========================================

List<ActivityLog> history = activityLogRepository
    .findByIncidentIdOrderByTimestampDesc(incident.getId());

for (ActivityLog log : history) {
    System.out.println(log.getTimestamp() + " - " + 
                       log.getAction() + " by " + 
                       log.getPerformedBy());
}
// Output:
// 2025-01-15 14:35:00 - RESOLVED by sarah.chen@bae.com
// 2025-01-15 14:30:00 - COMMENT_ADDED by sarah.chen@bae.com
// 2025-01-15 14:15:00 - COMMENT_ADDED by sarah.chen@bae.com
// 2025-01-15 14:10:00 - INVESTIGATION_STARTED by sarah.chen@bae.com
// 2025-01-15 14:05:00 - ACKNOWLEDGED by sarah.chen@bae.com
```

---

## Auto-Escalation Example

Scheduled job that runs every minute:
```java
@Scheduled(fixedRate = 60000) // Every 60 seconds
public void autoEscalateIncidents() {
    // Find incidents reported >5 minutes ago and still unacknowledged
    LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
    
    List<Incident> needsEscalation = incidentRepository
        .findIncidentsNeedingEscalation(IncidentState.REPORTED, fiveMinutesAgo);
    
    for (Incident incident : needsEscalation) {
        incident.escalate(); // Priority: LOW → MEDIUM → HIGH → CRITICAL
        incidentRepository.save(incident);
        // ActivityLog: "ESCALATED by SYSTEM - Priority escalated from X to Y"
    }
}
```

---

## Testing Examples

These examples can be used in unit tests:
```java
@Test
void testIncidentWorkflow() {
    // Create incident
    Incident incident = Incident.builder()
        .title("Test Incident")
        .severity(Severity.HIGH)
        .reportedBy("test-user")
        .build();
    
    // Verify initial state
    assertEquals(IncidentState.REPORTED, incident.getState());
    assertNull(incident.getAssignedTo());
    
    // Acknowledge
    incident.acknowledge("responder@test.com");
    assertEquals(IncidentState.ACKNOWLEDGED, incident.getState());
    assertEquals("responder@test.com", incident.getAssignedTo());
    assertNotNull(incident.getAcknowledgedAt());
    
    // Start investigation
    incident.startInvestigation("responder@test.com");
    assertEquals(IncidentState.INVESTIGATING, incident.getState());
    
    // Resolve
    incident.resolve("responder@test.com");
    assertEquals(IncidentState.RESOLVED, incident.getState());
    assertNotNull(incident.getResolvedAt());
}
```

---

## Common Patterns

### Pattern 1: Auto-Assignment to On-Call Responder
```java
public void autoAssignIncident(Incident incident) {
    List<Responder> oncall = responderRepository.findByOnCallTrue();
    
    if (!oncall.isEmpty()) {
        Responder responder = oncall.get(0);
        incident.acknowledge(responder.getEmail());
        incidentRepository.save(incident);
    }
}
```

### Pattern 2: Priority Calculation Based on Severity and Impact
```java
public Priority calculatePriority(Severity severity, int affectedSystems) {
    if (severity == Severity.CRITICAL || affectedSystems > 10) {
        return Priority.CRITICAL;
    } else if (severity == Severity.HIGH || affectedSystems > 5) {
        return Priority.HIGH;
    } else if (severity == Severity.MEDIUM) {
        return Priority.MEDIUM;
    }
    return Priority.LOW;
}
```

### Pattern 3: Metrics Dashboard Queries
```java
// Count by severity
Map<Severity, Long> bySeverity = incidentRepository.findAll().stream()
    .collect(Collectors.groupingBy(Incident::getSeverity, Collectors.counting()));

// Count by state
Map<IncidentState, Long> byState = incidentRepository.findAll().stream()
    .collect(Collectors.groupingBy(Incident::getState, Collectors.counting()));

// Average resolution time
Duration avgResolutionTime = incidentRepository.findAll().stream()
    .filter(i -> i.getResolvedAt() != null)
    .map(i -> Duration.between(i.getReportedAt(), i.getResolvedAt()))
    .collect(Collectors.averagingLong(Duration::toMinutes));
```

---

## BAE Systems Interview Talking Points

### Why This Architecture?
"I used a layered architecture with clear separation of concerns - entities define the data model, repositories handle data access, services contain business logic, and controllers expose REST APIs. This mirrors the modular design patterns used in command and control systems where each layer can be tested and deployed independently."

### Why Enums for State Machine?
"The incident workflow uses enums to enforce valid states and transitions. Similar to weapon system states (SAFE, ARMED, FIRING) or mission planning workflows in defense software, invalid transitions must be prevented for safety and data integrity. The enum-based state machine makes illegal states unrepresentable."

### Why Complete Audit Trail?
"Every action creates an ActivityLog entry with timestamp and performer. Defense software requires complete audit trails for compliance, security, and after-action review. The ActivityLog entity captures who did what and when - essential for both security audits and operational analysis in classified environments."

### Why Auto-Escalation?
"The scheduled task monitors SLA violations and auto-escalates priority. In tactical systems, automated monitoring is critical - you can't rely on humans to constantly check status. The system escalates priority automatically based on time thresholds, similar to how C2 systems auto-generate alerts for time-critical events."