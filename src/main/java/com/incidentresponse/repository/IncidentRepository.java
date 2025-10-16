package com.incidentresponse.repository;

import com.incidentresponse.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, UUID> {
    
    // Spring Data JPA generates these queries automatically!
    List<Incident> findByState(IncidentState state);
    List<Incident> findBySeverity(Severity severity);
    List<Incident> findByAssignedTo(String assignedTo);
    List<Incident> findByReportedBy(String reportedBy);
    List<Incident> findByStateNotIn(List<IncidentState> states);
    List<Incident> findByStateAndAcknowledgedAtIsNull(IncidentState state);
    
    List<Incident> findByPriorityInAndStateNotIn(
        List<Priority> priorities, 
        List<IncidentState> excludedStates
    );
    
    // Custom query for auto-escalation
    @Query("SELECT i FROM Incident i WHERE i.state = :state " +
           "AND i.acknowledgedAt IS NULL " +
           "AND i.reportedAt < :threshold")
    List<Incident> findIncidentsNeedingEscalation(
        @Param("state") IncidentState state,
        @Param("threshold") LocalDateTime threshold
    );
    
    // Metrics query
    @Query("SELECT i.severity, COUNT(i) FROM Incident i " +
           "WHERE i.state IN :states GROUP BY i.severity")
    List<Object[]> countBySeverityAndState(@Param("states") List<IncidentState> states);
}
