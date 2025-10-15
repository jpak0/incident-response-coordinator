package com.incidentresponse.model; // puts class in model package (domian/entity layer)

import jakarta.persistence.*; // JPA annotations for database mapping
import lombok.*; // Lombok annotations for boilerplate code
import java.util.UUID; // UUID for unique identifiers

@Entity // tells JPA "this is a database table" each instance = one row in database, quieried with repo methods
@Table(name = "responders") // names database table, optional not necessary but good practice.
@Data // Lombok annotation for getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Lombok annotation for no-args constructor (empty)
@AllArgsConstructor // Lombok annotation for all-args constructor (all fields)
@Builder // Lombok annotation for builder pattern
public class Responder {
    
    @Id // primary key unique identifier per row
    // @GeneratorValue(strategy = GenerationType.UUID): auto-generates a UUID when saving to the database. UUID was chosen instead of a Long/Int value
    // because it is globally unique, offline usability, harder to guess, lack of collision risk, and through research I
    // found we commonly used in distributed systems.
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id; // stores unique identifier
    
    // creates database column called name; requires input for a person's name
    @Column(nullable = false)
    private String name;
    
    // creates database column called email; requires a unique email address
    @Column(nullable = false, unique = true)
    private String email;
    
    // creates database column called role; requires input for a person's job/role
    @Column(nullable = false)
    @Builder.Default
    private String role = "RESPONDER"; // default role is "RESPONDER", can be overwritten
    // Note: this is a good example of using the builder pattern to set the role
    // Uses default role ("RESPONDER")
    //  Responder r1 = Responder.builder()
        // .name("John")
        // .email("john@example.com")
        // .build();
    // r1.getRole() → "RESPONDER"

    // Override the role
    // Responder r2 = Responder.builder()
        // .name("Sarah")
        // .email("sarah@example.com")
        // .role("ENGINEER")
        // .build();
    // r2.getRole() → "ENGINEER"

    // creates database column called onCall; requires input for a person's on-call status
    @Column(nullable = false)
    @Builder.Default
    private boolean onCall = false;

    // EX). Usage for onCall:
    
    // Creating an on-call responder
    //Responder oncall = Responder.builder()
        //.name("Emergency Responder")
        //.email("oncall@example.com")
        //.onCall(true)  // Override default
        //.build();

// Query for on-call responders
//List<Responder> available = responderRepository.findByOnCallTrue();
// Returns only responders where onCall = true

// DATABASE VISUAL:
//Responder responder = Responder.builder()
    //.name("John Doe")
    //.email("john.doe@bae.com")
    //.role("ENGINEER")
    //.onCall(true)
    //.build();

//responderRepository.save(responder);
//```

//Database table `responders`:
//```
//+--------------------------------------+----------+-------------------+----------+---------+
//| id                                   | name     | email             | role     | on_call |
//+--------------------------------------+----------+-------------------+----------+---------+
//| 550e8400-e29b-41d4-a716-446655440000 | John Doe | john.doe@bae.com  | ENGINEER | true    |
//+--------------------------------------+----------+-------------------+----------+---------+

}
