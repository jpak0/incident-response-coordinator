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
@Builder           // ← THIS IS CRITICAL - Enables Comment.builder()
public class Comment {
    
    // PRIMARY KEY: Unique identifier for each comment
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // FOREIGN KEY: Links this comment to its parent incident
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    @ToString.Exclude
    private Incident incident;

    // AUTHOR: Who wrote this comment
    @Column(nullable = false)
    private String author;

    // CONTENT: The actual comment text
    @Column(length = 2000, nullable = false)
    private String content;

    // TIMESTAMP: When this comment was created
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // SYSTEM FLAG: Auto-generated or human-written?
    @Column(nullable = false)
    @Builder.Default
    private boolean isSystemGenerated = false;
}