package com.incidentresponse.repository;

import com.incidentresponse.model.Responder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResponderRepository extends JpaRepository<Responder, UUID> {
    Optional<Responder> findByEmail(String email);
    List<Responder> findByOnCallTrue();
    List<Responder> findByRole(String role);
}
