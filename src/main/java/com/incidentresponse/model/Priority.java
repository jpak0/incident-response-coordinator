package com.incidentresponse.model; // puts class in model package (domian/entity layer)

// we can change the values of the enum to match the priority of the incident
// enum specifically was used because it is a fixed set of values that cannot be changed
// and it is used to represent the priority of the incident, this is important in the 
// context of a real-world incident response workflow database, where invalid input could
// be a security risk, and cause an undefined state just because of a typo. This choice reflects
// data integrity, validation, and self documentation.

public enum Priority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
