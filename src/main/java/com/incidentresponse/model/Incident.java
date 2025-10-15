package com.incidentresponse.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// MAIN ENTITY: Represents a security/operational incident
// This is the core of the system - everything revolves around incidents
@Entity
@Table(name = "incidents")

// LOMBOK ANNOTATIONS
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {
    
    // ========================================
    // CORE FIELDS: Basic incident information
    // ========================================
    
    // PRIMARY KEY: Unique identifier for this incident
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // TITLE: Short description (required)
    // @NotBlank = Cannot be null, empty, or just whitespace
    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;

    // DESCRIPTION: Detailed explanation (optional)
    // length = 2000 allows longer text
    @Column(length = 2000)
    private String description;

    // SEVERITY: How bad is this? (required)
    // LOW, MEDIUM, HIGH, CRITICAL
    // @Enumerated(STRING) = Store "CRITICAL" not "3" in database
    @NotNull(message = "Severity is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    // ========================================
    // WORKFLOW FIELDS: Track incident lifecycle
    // ========================================
    
    // STATE: Where is this incident in the workflow? (required)
    // REPORTED → ACKNOWLEDGED → INVESTIGATING → MITIGATING → RESOLVED → CLOSED
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default  // Default to REPORTED when creating new incident
    private IncidentState state = IncidentState.REPORTED;

    // PRIORITY: How urgent is this? (required, defaults to MEDIUM)
    // Can be auto-escalated based on age and severity
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    // ========================================
    // TIMESTAMP FIELDS: Track key moments
    // ========================================
    
    // REPORTED_AT: When was this incident first created?
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime reportedAt = LocalDateTime.now();

    // ACKNOWLEDGED_AT: When did someone take ownership?
    // Null = not yet acknowledged
    private LocalDateTime acknowledgedAt;

    // RESOLVED_AT: When was the problem fixed?
    // Null = not yet resolved
    private LocalDateTime resolvedAt;

    // CLOSED_AT: When was this archived?
    // Null = still open
    private LocalDateTime closedAt;

    // ========================================
    // PEOPLE FIELDS: Who's involved?
    // ========================================
    
    // REPORTED_BY: Who reported this incident? (required)
    // Could be person's name, email, or "MONITORING_SYSTEM"
    @NotBlank(message = "Reporter is required")
    @Column(nullable = false)
    private String reportedBy;

    // ASSIGNED_TO: Who is currently working on this?
    // Null = unassigned, set when acknowledged
    private String assignedTo;

    // ========================================
    // IMPACT FIELD: How many systems affected?
    // ========================================
    
    // AFFECTED_SYSTEMS_COUNT: Number of impacted systems/users
    // Used to calculate priority (more systems = higher priority)
    @Column(nullable = false)
    @Builder.Default
    private Integer affectedSystemsCount = 1;

    // ========================================
    // RELATIONSHIPS: Associated data
    // ========================================
    
    // COMMENTS: List of all comments on this incident
    // @OneToMany = One incident has many comments
    // mappedBy = "incident" means Comment.incident field owns the relationship
    // cascade = ALL means save/delete incident → save/delete comments
    // orphanRemoval = true means removing comment from list → deletes from DB
    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    // ACTIVITY_LOGS: Complete audit trail of all actions
    // Same relationship pattern as comments
    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ActivityLog> activityLogs = new ArrayList<>();

    // ========================================
    // BUSINESS LOGIC METHODS: State transitions
    // ========================================
    
    /**
     * ACKNOWLEDGE: Assign incident to a responder
     * Can only acknowledge if in REPORTED state
     * 
     * Example:
     *   incident.acknowledge("john.doe@bae.com");
     *   // state: REPORTED → ACKNOWLEDGED
     *   // assignedTo: null → "john.doe@bae.com"
     *   // acknowledgedAt: null → current time
     */
    public void acknowledge(String responder) {
        // VALIDATION: Can only acknowledge NEW incidents
        if (this.state != IncidentState.REPORTED) {
            throw new IllegalStateException("Can only acknowledge incidents in REPORTED state");
        }
        
        // STATE TRANSITION
        this.state = IncidentState.ACKNOWLEDGED;
        this.acknowledgedAt = LocalDateTime.now();
        this.assignedTo = responder;
        
        // AUDIT LOG: Record who acknowledged it
        addActivityLog("ACKNOWLEDGED", responder, "Incident acknowledged and assigned");
    }

    /**
     * START_INVESTIGATION: Begin active work on the incident
     * Can only investigate if incident has been acknowledged
     * 
     * Example:
     *   incident.startInvestigation("john.doe@bae.com");
     *   // state: ACKNOWLEDGED → INVESTIGATING
     */
    public void startInvestigation(String responder) {
        // VALIDATION: Must be acknowledged first
        if (this.state != IncidentState.ACKNOWLEDGED) {
            throw new IllegalStateException("Can only start investigation after acknowledgment");
        }
        
        // STATE TRANSITION
        this.state = IncidentState.INVESTIGATING;
        
        // AUDIT LOG
        addActivityLog("INVESTIGATION_STARTED", responder, "Investigation started");
    }

    /**
     * RESOLVE: Mark incident as fixed
     * Can only resolve if actively working on it
     * 
     * Example:
     *   incident.resolve("john.doe@bae.com");
     *   // state: INVESTIGATING → RESOLVED
     *   // resolvedAt: null → current time
     */
    public void resolve(String responder) {
        // VALIDATION: Must be investigating or mitigating
        if (this.state != IncidentState.INVESTIGATING && this.state != IncidentState.MITIGATING) {
            throw new IllegalStateException("Must be investigating or mitigating to resolve");
        }
        
        // STATE TRANSITION
        this.state = IncidentState.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
        
        // AUDIT LOG
        addActivityLog("RESOLVED", responder, "Incident resolved");
    }

    /**
     * ESCALATE: Increase priority automatically
     * LOW → MEDIUM → HIGH → CRITICAL
     * Called by scheduled job for old unacknowledged incidents
     * 
     * Example:
     *   incident.escalate();
     *   // priority: MEDIUM → HIGH
     *   // Creates activity log entry
     */
    public void escalate() {
        Priority oldPriority = this.priority;
        
        // PRIORITY LADDER: Move up one level
        this.priority = switch (this.priority) {
            case LOW -> Priority.MEDIUM;
            case MEDIUM -> Priority.HIGH;
            case HIGH -> Priority.CRITICAL;
            case CRITICAL -> Priority.CRITICAL; // Already at max
        };
        
        // AUDIT LOG: Only if priority actually changed
        if (oldPriority != this.priority) {
            addActivityLog("ESCALATED", "SYSTEM",
                "Priority escalated from " + oldPriority + " to " + this.priority);
        }
    }

    /**
     * ADD_COMMENT: Add a human or system comment
     * 
     * Example:
     *   incident.addComment("john.doe", "Identified root cause: memory leak");
     *   // Creates Comment and ActivityLog entries
     */
    public void addComment(String author, String content) {
        // CREATE COMMENT OBJECT
        Comment comment = Comment.builder()
            .incident(this)              // Link to this incident
            .author(author)
            .content(content)
            .timestamp(LocalDateTime.now())
            .isSystemGenerated(false)    // Human comment
            .build();
        
        // ADD TO LIST (JPA will save it automatically)
        this.comments.add(comment);
        
        // AUDIT LOG
        addActivityLog("COMMENT_ADDED", author, "Added comment");
    }

    /**
     * ADD_ACTIVITY_LOG: Internal helper method
     * Automatically called by other methods to create audit trail
     * 
     * Example:
     *   addActivityLog("RESOLVED", "john.doe", "Issue fixed");
     *   // Creates ActivityLog entry automatically
     */
    private void addActivityLog(String action, String performedBy, String details) {
        // CREATE LOG ENTRY
        ActivityLog log = ActivityLog.builder()
            .incident(this)
            .action(action)
            .performedBy(performedBy)
            .details(details)
            .timestamp(LocalDateTime.now())
            .build();
        
        // ADD TO LIST (JPA will save it automatically)
        this.activityLogs.add(log);
    }
}

// EXAMPLE USAGE: Incident Class
// ```
// 1. CREATE INCIDENT
//Incident incident = Incident.builder()
//    .title("Database connection timeout")
//    .severity(Severity.CRITICAL)
//    .reportedBy("monitoring-system")
//    .affectedSystemsCount(5)
//    .build();
// State: REPORTED, Priority: MEDIUM (default)

// 2. ACKNOWLEDGE (assign to responder)
//incident.acknowledge("john.doe@bae.com");
// State: REPORTED → ACKNOWLEDGED
// ActivityLog: "john.doe@bae.com ACKNOWLEDGED incident"

// 3. START WORKING
//incident.startInvestigation("john.doe@bae.com");
// State: ACKNOWLEDGED → INVESTIGATING

// 4. ADD UPDATES
//incident.addComment("john.doe@bae.com", "Found memory leak in connection pool");
// Comment added + ActivityLog entry

// 5. AUTO-ESCALATION (if taking too long)
//incident.escalate();
// Priority: MEDIUM → HIGH
// ActivityLog: "SYSTEM ESCALATED priority"

// 6. RESOLVE
//incident.resolve("john.doe@bae.com");
// State: INVESTIGATING → RESOLVED
// resolvedAt: current timestamp
// ActivityLog: "john.doe@bae.com RESOLVED incident"
//```
 
// Key Concepts Summary

// **Relationships:**
//```
// Incident (1) ←→ (Many) Comments
// Incident (1) ←→ (Many) ActivityLogs
// ```
// - One incident has many comments and logs
// - Each comment/log belongs to exactly one incident

// ### **State Machine:**
// ```
// REPORTED → ACKNOWLEDGED → INVESTIGATING → MITIGATING → RESOLVED → CLOSED
// ```
// - Can't skip states (enforced by validation)
// - Each transition creates audit log

// ### **Auto-Escalation:**
// ```
// Scheduled job checks every minute:
// IF (state == REPORTED && age > 5 minutes)
//  THEN escalate priority