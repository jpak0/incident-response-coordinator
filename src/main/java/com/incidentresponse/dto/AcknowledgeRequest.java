package com.incidentresponse.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for acknowledging an incident.
 * Used by PUT /api/incidents/{id}/acknowledge endpoint.
 * Assigns incident to a responder and transitions state from REPORTED to ACKNOWLEDGED.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcknowledgeRequest {
    
    /**
     * Name or email of the responder taking ownership of this incident (required).
     * Example: "sarah.chen@bae.com" or "Sarah Chen"
     */
    @NotBlank(message = "Responder name is required")
    private String responder;
}