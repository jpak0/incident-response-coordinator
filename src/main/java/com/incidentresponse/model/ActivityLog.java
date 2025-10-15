package com.incidentresponse.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

// DATABASE TABLE: Creates "activity_logs" table for audit trail
// Every action on an incident creates a log entry (who, what, when)
@Entity
@Table(name = "activity_logs")

// LOMBOK: Auto-generates boilerplate code
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {
    
    // PRIMARY KEY: Unique ID for each log entry
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // FOREIGN KEY: Which incident does this log belong to?
    // Many logs can belong to one incident (complete history)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    @ToString.Exclude
    private Incident incident;

    // ACTION: What happened? Examples:
    // "ACKNOWLEDGED", "RESOLVED", "ESCALATED", "COMMENT_ADDED"
    @Column(nullable = false)
    private String action;

    // PERFORMED_BY: Who did this action?
    // Could be username, email, or "SYSTEM" for automated actions
    @Column(nullable = false)
    private String performedBy;

    // DETAILS: Additional context about the action
    // Example: "Priority escalated from MEDIUM to HIGH"
    @Column(length = 1000)
    private String details;

    // TIMESTAMP: Exactly when this action occurred
    // Critical for audit trails and compliance
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}

// Log when someone acknowledges an incident
//ActivityLog log1 = ActivityLog.builder()
//    .incident(myIncident)
//    .action("ACKNOWLEDGED")
//    .performedBy("sarah.chen@bae.com")
//    .details("Incident assigned to security team")
//    .build();

// Log when system auto-escalates
//ActivityLog log2 = ActivityLog.builder()
//    .incident(myIncident)
//    .action("ESCALATED")
//    .performedBy("SYSTEM")
//    .details("Priority escalated from LOW to MEDIUM (SLA breach)")
//    .build();