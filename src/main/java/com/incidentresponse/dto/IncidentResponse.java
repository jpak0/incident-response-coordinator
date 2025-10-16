package com.incidentresponse.dto;

import com.incidentresponse.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for incident data.
 * Used by all GET endpoints that return incident information.
 * Hides internal entity details and exposes only necessary data to API clients.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResponse {
    
    /**
     * Unique identifier for this incident.
     * Example: "550e8400-e29b-41d4-a716-446655440000"
     */
    private UUID id;
    
    /**
     * Brief incident description.
     */
    private String title;
    
    /**
     * Detailed incident explanation.
     */
    private String description;
    
    /**
     * Incident severity level.
     * Values: LOW, MEDIUM, HIGH, CRITICAL
     */
    private Severity severity;
    
    /**
     * Current workflow state.
     * Values: REPORTED, ACKNOWLEDGED, INVESTIGATING, MITIGATING, RESOLVED, CLOSED
     */
    private IncidentState state;
    
    /**
     * Response urgency level (can be auto-escalated).
     * Values: LOW, MEDIUM, HIGH, CRITICAL
     */
    private Priority priority;
    
    /**
     * When this incident was first reported.
     */
    private LocalDateTime reportedAt;
    
    /**
     * When this incident was acknowledged by a responder (null if not yet acknowledged).
     */
    private LocalDateTime acknowledgedAt;
    
    /**
     * When this incident was resolved (null if not yet resolved).
     */
    private LocalDateTime resolvedAt;
    
    /**
     * Who reported this incident.
     * Example: "monitoring-system" or "john.doe@bae.com"
     */
    private String reportedBy;
    
    /**
     * Who is currently assigned to this incident (null if unassigned).
     * Example: "sarah.chen@bae.com"
     */
    private String assignedTo;
    
    /**
     * Number of systems/services affected.
     */
    private Integer affectedSystemsCount;
    
    /**
     * Total number of comments on this incident.
     * Useful for UI badges showing comment count.
     */
    private Integer commentCount;
}