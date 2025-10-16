package com.incidentresponse.controller;

import com.incidentresponse.dto.*;
import com.incidentresponse.model.IncidentState;
import com.incidentresponse.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Incident Management API.
 * Exposes HTTP endpoints for creating, reading, and updating incidents.
 * All endpoints are prefixed with /api/incidents
 */
@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@Slf4j
public class IncidentController {

    // Service layer injected automatically by Spring
    private final IncidentService incidentService;

    // ========================================
    // CREATE ENDPOINTS
    // ========================================

    /**
     * Creates a new incident.
     * 
     * POST /api/incidents
     * 
     * Request body example:
     * {
     *   "title": "Production Database Down",
     *   "description": "Unable to connect to primary database",
     *   "severity": "CRITICAL",
     *   "reportedBy": "monitoring-system",
     *   "affectedSystemsCount": 5
     * }
     * 
     * @param request CreateIncidentRequest DTO (validated automatically)
     * @return 201 Created with IncidentResponse
     */
    @PostMapping
    public ResponseEntity<IncidentResponse> createIncident(
            @Valid @RequestBody CreateIncidentRequest request) {
        
        log.info("Received request to create incident: {}", request.getTitle());
        
        IncidentResponse response = incidentService.createIncident(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ========================================
    // READ ENDPOINTS
    // ========================================

    /**
     * Gets all incidents in the system.
     * 
     * GET /api/incidents
     * 
     * @return 200 OK with list of all incidents
     */
    @GetMapping
    public ResponseEntity<List<IncidentResponse>> getAllIncidents() {
        log.debug("Received request to get all incidents");
        
        List<IncidentResponse> incidents = incidentService.getAllIncidents();
        
        return ResponseEntity.ok(incidents);
    }

    /**
     * Gets a single incident by ID.
     * 
     * GET /api/incidents/{id}
     * 
     * @param id UUID of the incident
     * @return 200 OK with incident data, or 404 Not Found if doesn't exist
     */
    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> getIncidentById(@PathVariable UUID id) {
        log.debug("Received request to get incident: {}", id);
        
        IncidentResponse incident = incidentService.getIncidentById(id);
        
        return ResponseEntity.ok(incident);
    }

    /**
     * Gets all active incidents (not RESOLVED or CLOSED).
     * Useful for dashboards showing current workload.
     * 
     * GET /api/incidents/active
     * 
     * @return 200 OK with list of active incidents
     */
    @GetMapping("/active")
    public ResponseEntity<List<IncidentResponse>> getActiveIncidents() {
        log.debug("Received request to get active incidents");
        
        List<IncidentResponse> incidents = incidentService.getActiveIncidents();
        
        return ResponseEntity.ok(incidents);
    }

    /**
     * Gets incidents by state.
     * 
     * GET /api/incidents/state/{state}
     * 
     * Example: GET /api/incidents/state/INVESTIGATING
     * 
     * @param state IncidentState enum value
     * @return 200 OK with list of matching incidents
     */
    @GetMapping("/state/{state}")
    public ResponseEntity<List<IncidentResponse>> getIncidentsByState(
            @PathVariable IncidentState state) {
        
        log.debug("Received request to get incidents with state: {}", state);
        
        List<IncidentResponse> incidents = incidentService.getIncidentsByState(state);
        
        return ResponseEntity.ok(incidents);
    }

    /**
     * Gets incidents assigned to a specific responder.
     * 
     * GET /api/incidents/responder/{email}
     * 
     * Example: GET /api/incidents/responder/john.doe@bae.com
     * 
     * @param email Responder's email address
     * @return 200 OK with list of assigned incidents
     */
    @GetMapping("/responder/{email}")
    public ResponseEntity<List<IncidentResponse>> getIncidentsByResponder(
            @PathVariable String email) {
        
        log.debug("Received request to get incidents for responder: {}", email);
        
        List<IncidentResponse> incidents = incidentService.getIncidentsByResponder(email);
        
        return ResponseEntity.ok(incidents);
    }

    // ========================================
    // UPDATE ENDPOINTS (State Transitions)
    // ========================================

    /**
     * Acknowledges an incident and assigns it to a responder.
     * Transitions state from REPORTED → ACKNOWLEDGED
     * 
     * PUT /api/incidents/{id}/acknowledge
     * 
     * Request body:
     * {
     *   "responder": "john.doe@bae.com"
     * }
     * 
     * @param id Incident ID
     * @param request AcknowledgeRequest with responder info
     * @return 200 OK with updated incident, or 404/400 on error
     */
    @PutMapping("/{id}/acknowledge")
    public ResponseEntity<IncidentResponse> acknowledgeIncident(
            @PathVariable UUID id,
            @Valid @RequestBody AcknowledgeRequest request) {
        
        log.info("Received request to acknowledge incident {} by {}", 
            id, request.getResponder());
        
        IncidentResponse response = incidentService.acknowledgeIncident(id, request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Starts investigation on an incident.
     * Transitions state from ACKNOWLEDGED → INVESTIGATING
     * 
     * PUT /api/incidents/{id}/investigate
     * 
     * Request body:
     * {
     *   "responder": "john.doe@bae.com"
     * }
     * 
     * @param id Incident ID
     * @param request Contains responder email
     * @return 200 OK with updated incident
     */
    @PutMapping("/{id}/investigate")
    public ResponseEntity<IncidentResponse> startInvestigation(
            @PathVariable UUID id,
            @Valid @RequestBody AcknowledgeRequest request) {
        
        log.info("Received request to start investigation on incident {} by {}", 
            id, request.getResponder());
        
        IncidentResponse response = incidentService.startInvestigation(
            id, request.getResponder());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Resolves an incident.
     * Transitions state from INVESTIGATING/MITIGATING → RESOLVED
     * 
     * PUT /api/incidents/{id}/resolve
     * 
     * Request body:
     * {
     *   "responder": "john.doe@bae.com"
     * }
     * 
     * @param id Incident ID
     * @param request Contains responder email
     * @return 200 OK with updated incident
     */
    @PutMapping("/{id}/resolve")
    public ResponseEntity<IncidentResponse> resolveIncident(
            @PathVariable UUID id,
            @Valid @RequestBody AcknowledgeRequest request) {
        
        log.info("Received request to resolve incident {} by {}", 
            id, request.getResponder());
        
        IncidentResponse response = incidentService.resolveIncident(
            id, request.getResponder());
        
        return ResponseEntity.ok(response);
    }

    // ========================================
    // COMMENT ENDPOINTS
    // ========================================

    /**
     * Adds a comment to an incident.
     * 
     * POST /api/incidents/{id}/comments
     * 
     * Request body:
     * {
     *   "author": "john.doe@bae.com",
     *   "content": "Identified root cause: memory leak"
     * }
     * 
     * @param id Incident ID
     * @param request AddCommentRequest with author and content
     * @return 200 OK with updated incident
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity<IncidentResponse> addComment(
            @PathVariable UUID id,
            @Valid @RequestBody AddCommentRequest request) {
        
        log.info("Received request to add comment to incident {} by {}", 
            id, request.getAuthor());
        
        IncidentResponse response = incidentService.addComment(id, request);
        
        return ResponseEntity.ok(response);
    }
}