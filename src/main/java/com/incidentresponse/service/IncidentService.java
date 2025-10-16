package com.incidentresponse.service;

import com.incidentresponse.dto.*;
import com.incidentresponse.exception.IncidentNotFoundException;
import com.incidentresponse.model.*;
import com.incidentresponse.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for incident management business logic.
 * Handles CRUD operations, state transitions, auto-escalation, and DTO conversions.
 * This is the "brain" of the application - all business rules live here.
 */
@Service
@RequiredArgsConstructor  // Lombok generates constructor for final fields (dependency injection)
@Slf4j                    // Lombok generates logger field: log.info(), log.error(), etc.
@Transactional            // All methods run in database transactions (rollback on error)
public class IncidentService {

    // Automatically injected by Spring (via @RequiredArgsConstructor)
    private final IncidentRepository incidentRepository;

    // ========================================
    // CREATE OPERATIONS
    // ========================================

    /**
     * Creates a new incident from API request.
     * Automatically calculates priority based on severity and affected systems.
     * 
     * @param request CreateIncidentRequest DTO from API
     * @return IncidentResponse DTO to send back to client
     */
    public IncidentResponse createIncident(CreateIncidentRequest request) {
        log.info("Creating new incident: {}", request.getTitle());
        
        // Calculate initial priority based on severity and impact
        Priority calculatedPriority = calculatePriority(
            request.getSeverity(), 
            request.getAffectedSystemsCount()
        );
        
        // Build incident entity from request DTO
        Incident incident = Incident.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .severity(request.getSeverity())
            .priority(calculatedPriority)
            .reportedBy(request.getReportedBy())
            .affectedSystemsCount(request.getAffectedSystemsCount())
            .build();
        
        // Save to database
        Incident savedIncident = incidentRepository.save(incident);
        
        log.info("Incident created with ID: {}", savedIncident.getId());
        
        // Convert entity to response DTO
        return convertToResponse(savedIncident);
    }

    // ========================================
    // READ OPERATIONS
    // ========================================

    /**
     * Gets a single incident by ID.
     * 
     * @param id UUID of the incident
     * @return IncidentResponse DTO
     * @throws IncidentNotFoundException if incident doesn't exist
     */
    public IncidentResponse getIncidentById(UUID id) {
        log.debug("Fetching incident with ID: {}", id);
        
        Incident incident = incidentRepository.findById(id)
            .orElseThrow(() -> new IncidentNotFoundException("Incident not found with ID: " + id));
        
        return convertToResponse(incident);
    }

    /**
     * Gets all incidents in the system.
     * 
     * @return List of IncidentResponse DTOs
     */
    public List<IncidentResponse> getAllIncidents() {
        log.debug("Fetching all incidents");
        
        return incidentRepository.findAll().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Gets all active incidents (not RESOLVED or CLOSED).
     * Useful for dashboards showing current workload.
     * 
     * @return List of active incidents
     */
    public List<IncidentResponse> getActiveIncidents() {
        log.debug("Fetching active incidents");
        
        List<IncidentState> inactiveStates = Arrays.asList(
            IncidentState.RESOLVED, 
            IncidentState.CLOSED
        );
        
        return incidentRepository.findByStateNotIn(inactiveStates).stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Gets incidents by current state.
     * Example: Get all INVESTIGATING incidents
     * 
     * @param state IncidentState to filter by
     * @return List of matching incidents
     */
    public List<IncidentResponse> getIncidentsByState(IncidentState state) {
        log.debug("Fetching incidents with state: {}", state);
        
        return incidentRepository.findByState(state).stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Gets incidents assigned to a specific responder.
     * 
     * @param responderEmail Email of the responder
     * @return List of assigned incidents
     */
    public List<IncidentResponse> getIncidentsByResponder(String responderEmail) {
        log.debug("Fetching incidents assigned to: {}", responderEmail);
        
        return incidentRepository.findByAssignedTo(responderEmail).stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    // ========================================
    // STATE TRANSITION OPERATIONS
    // ========================================

    /**
     * Acknowledges an incident and assigns it to a responder.
     * Transitions: REPORTED → ACKNOWLEDGED
     * 
     * @param id Incident ID
     * @param request AcknowledgeRequest containing responder info
     * @return Updated IncidentResponse
     * @throws IncidentNotFoundException if incident doesn't exist
     * @throws IllegalStateException if incident is not in REPORTED state
     */
    public IncidentResponse acknowledgeIncident(UUID id, AcknowledgeRequest request) {
        log.info("Acknowledging incident {} by {}", id, request.getResponder());
        
        Incident incident = incidentRepository.findById(id)
            .orElseThrow(() -> new IncidentNotFoundException("Incident not found with ID: " + id));
        
        // Business logic: state transition (validates state internally)
        incident.acknowledge(request.getResponder());
        
        Incident updatedIncident = incidentRepository.save(incident);
        
        log.info("Incident {} acknowledged successfully", id);
        
        return convertToResponse(updatedIncident);
    }

    /**
     * Starts investigation on an incident.
     * Transitions: ACKNOWLEDGED → INVESTIGATING
     * 
     * @param id Incident ID
     * @param responder Who is starting the investigation
     * @return Updated IncidentResponse
     * @throws IncidentNotFoundException if incident doesn't exist
     * @throws IllegalStateException if incident is not in ACKNOWLEDGED state
     */
    public IncidentResponse startInvestigation(UUID id, String responder) {
        log.info("Starting investigation on incident {} by {}", id, responder);
        
        Incident incident = incidentRepository.findById(id)
            .orElseThrow(() -> new IncidentNotFoundException("Incident not found with ID: " + id));
        
        incident.startInvestigation(responder);
        
        Incident updatedIncident = incidentRepository.save(incident);
        
        return convertToResponse(updatedIncident);
    }

    /**
     * Resolves an incident.
     * Transitions: INVESTIGATING/MITIGATING → RESOLVED
     * 
     * @param id Incident ID
     * @param responder Who is resolving the incident
     * @return Updated IncidentResponse
     * @throws IncidentNotFoundException if incident doesn't exist
     * @throws IllegalStateException if incident is not in valid state
     */
    public IncidentResponse resolveIncident(UUID id, String responder) {
        log.info("Resolving incident {} by {}", id, responder);
        
        Incident incident = incidentRepository.findById(id)
            .orElseThrow(() -> new IncidentNotFoundException("Incident not found with ID: " + id));
        
        incident.resolve(responder);
        
        Incident updatedIncident = incidentRepository.save(incident);
        
        log.info("Incident {} resolved successfully", id);
        
        return convertToResponse(updatedIncident);
    }

    // ========================================
    // COMMENT OPERATIONS
    // ========================================

    /**
     * Adds a comment to an incident.
     * Creates both Comment entity and ActivityLog entry.
     * 
     * @param id Incident ID
     * @param request AddCommentRequest with author and content
     * @return Updated IncidentResponse
     * @throws IncidentNotFoundException if incident doesn't exist
     */
    public IncidentResponse addComment(UUID id, AddCommentRequest request) {
        log.info("Adding comment to incident {} by {}", id, request.getAuthor());
        
        Incident incident = incidentRepository.findById(id)
            .orElseThrow(() -> new IncidentNotFoundException("Incident not found with ID: " + id));
        
        incident.addComment(request.getAuthor(), request.getContent());
        
        Incident updatedIncident = incidentRepository.save(incident);
        
        return convertToResponse(updatedIncident);
    }

    // ========================================
    // AUTO-ESCALATION (SCHEDULED JOB)
    // ========================================

    /**
     * Automatically escalates priority for old unacknowledged incidents.
     * Runs every 60 seconds (configured by @Scheduled).
     * 
     * Business Rule: If incident is REPORTED for >5 minutes without acknowledgment,
     * escalate priority: LOW → MEDIUM → HIGH → CRITICAL
     * 
     * This is critical for SLA compliance - ensures incidents don't go unnoticed.
     */
    @Scheduled(fixedRate = 60000) // Run every 60 seconds (60000 milliseconds)
    public void autoEscalateIncidents() {
        log.debug("Running auto-escalation job");
        
        // Find incidents reported more than 5 minutes ago that are still unacknowledged
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        
        List<Incident> incidentsNeedingEscalation = incidentRepository
            .findIncidentsNeedingEscalation(IncidentState.REPORTED, fiveMinutesAgo);
        
        if (!incidentsNeedingEscalation.isEmpty()) {
            log.info("Found {} incidents needing escalation", incidentsNeedingEscalation.size());
            
            for (Incident incident : incidentsNeedingEscalation) {
                Priority oldPriority = incident.getPriority();
                incident.escalate(); // Increases priority by one level
                incidentRepository.save(incident);
                
                log.info("Escalated incident {} from {} to {}", 
                    incident.getId(), oldPriority, incident.getPriority());
            }
        }
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    /**
     * Calculates initial priority based on severity and impact.
     * 
     * Business Rules:
     * - CRITICAL severity OR >10 systems affected = CRITICAL priority
     * - HIGH severity OR >5 systems affected = HIGH priority
     * - MEDIUM severity = MEDIUM priority
     * - Otherwise = LOW priority
     * 
     * @param severity Incident severity level
     * @param affectedSystems Number of systems affected
     * @return Calculated priority level
     */
    private Priority calculatePriority(Severity severity, Integer affectedSystems) {
        if (severity == Severity.CRITICAL || affectedSystems > 10) {
            return Priority.CRITICAL;
        } else if (severity == Severity.HIGH || affectedSystems > 5) {
            return Priority.HIGH;
        } else if (severity == Severity.MEDIUM) {
            return Priority.MEDIUM;
        }
        return Priority.LOW;
    }

    /**
     * Converts Incident entity to IncidentResponse DTO.
     * Hides internal entity details and exposes only API-friendly data.
     * 
     * @param incident Incident entity from database
     * @return IncidentResponse DTO for API response
     */
    private IncidentResponse convertToResponse(Incident incident) {
        return IncidentResponse.builder()
            .id(incident.getId())
            .title(incident.getTitle())
            .description(incident.getDescription())
            .severity(incident.getSeverity())
            .state(incident.getState())
            .priority(incident.getPriority())
            .reportedAt(incident.getReportedAt())
            .acknowledgedAt(incident.getAcknowledgedAt())
            .resolvedAt(incident.getResolvedAt())
            .reportedBy(incident.getReportedBy())
            .assignedTo(incident.getAssignedTo())
            .affectedSystemsCount(incident.getAffectedSystemsCount())
            .commentCount(incident.getComments().size()) // Computed field
            .build();
    }
}