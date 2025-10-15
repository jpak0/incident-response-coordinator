package com.incidentresponse.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

// DATABASE TABLE: This class becomes the "comments" table
@Entity
@Table(name = "comments")

// LOMBOK: Auto-generates getters, setters, toString, equals, hashCode
@Data
@NoArgsConstructor  // Empty constructor (required by JPA)
@AllArgsConstructor // Constructor with all fields
@Builder           // Enables: Comment.builder().author("John").content("Fixed").build()
public class Comment {
    
    // PRIMARY KEY: Unique identifier for each comment
    // Auto-generated UUID like: "550e8400-e29b-41d4-a716-446655440000"
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // FOREIGN KEY: Links this comment to its parent incident
    // @ManyToOne = Many comments can belong to one incident
    // @JoinColumn = Database column name that stores the incident's ID
    // fetch = LAZY = Don't load the full incident unless we ask for it (performance)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    @ToString.Exclude  // Don't include incident in toString() to avoid infinite loops
    private Incident incident;

    // AUTHOR: Who wrote this comment (name or email)
    // nullable = false means database won't allow null/empty values
    @Column(nullable = false)
    private String author;

    // CONTENT: The actual comment text
    // length = 2000 allows up to 2000 characters
    @Column(length = 2000, nullable = false)
    private String content;

    // TIMESTAMP: When this comment was created
    // @Builder.Default = If not specified in builder, use LocalDateTime.now()
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // SYSTEM FLAG: Was this auto-generated or human-written?
    // true = System generated (e.g., "Priority escalated")
    // false = Human wrote it (default)
    @Column(nullable = false)
    @Builder.Default
    private boolean isSystemGenerated = false;
}

// Someone adds a comment
//Comment userComment = Comment.builder()
//    .incident(myIncident)
//    .author("john.doe@bae.com")
//    .content("Database connection pool increased to 50")
//    .isSystemGenerated(false)
//    .build();

// System auto-generates a comment
//Comment systemComment = Comment.builder()
//    .incident(myIncident)
//    .author("SYSTEM")
//    .content("Incident priority escalated from HIGH to CRITICAL")
//    .isSystemGenerated(true)
//    .build();