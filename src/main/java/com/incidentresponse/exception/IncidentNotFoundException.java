package com.incidentresponse.exception;

/**
 * Exception thrown when an incident is not found.
 * Used by GET /api/incidents/{id} endpoint.
 */
public class IncidentNotFoundException extends RuntimeException {
    
    /**
     * Constructor for IncidentNotFoundException.
     * @param message the message to display
     */
    public IncidentNotFoundException(String message) {
        super(message);
    }
}
