package com.incidentresponse.dto;

import com.incidentresponse.model.Severity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new incident.
 * Used by POST /api/incidents endpoint.
 * Validates required fields before creating incident entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateIncidentRequest {
    
   /** Brief description of incident (required)
    *  Ex). "production database connection timeout."
    */
    @NotBlank(message = "Title is required")
    private String title;
    
   /** Detailed explaination of incident (optionall)
    *  Ex). "database cluster not responding to queries, multiple services affected."
    */
    private String description;
    
   /** Severity of incident (required)
    *  Ex). "CRITICAL" (LOW, MEDIUM, HIGH, CRITICAL)
    */
    @NotNull(message = "Severity is required")
    private Severity severity;
    
   /** Reporter of incident (required)
    *  Ex). "john.doe@bae.com" (email, name, system identifier)
    */
    @NotBlank(message = "Reporter is required")
    private String reportedBy;
    
   /** Number of systems affected (required)
    *  Ex). 5 (minimum of 1 system must be affected, used for priority calculation)
    *  DEFAULT: 1
    */
    @Min(value = 1, message = "At least one system must be affected")
    @Builder.Default
    private Integer affectedSystemsCount = 1;
}
