package com.incidentresponse.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for adding a comment to an incident.
 * Used by POST /api/incidents/{id}/comments endpoint.
 * Creates a new comment and activity log entry.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddCommentRequest {
    
    /**
     * Name or email of the comment author (required).
     * Example: "john.doe@bae.com"
     */
    @NotBlank(message = "Author is required")
    private String author;
    
    /**
     * The comment text (required).
     * Example: "Identified root cause: memory leak in connection pool"
     */
    @NotBlank(message = "Comment content is required")
    private String content;
}