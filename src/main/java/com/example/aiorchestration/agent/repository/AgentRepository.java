package com.example.aiorchestration.agent.repository;

import com.example.aiorchestration.agent.model.Agent;
import com.example.aiorchestration.agent.model.AgentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgentRepository extends JpaRepository<Agent, String> {
    List<Agent> findByStatus(AgentStatus status);
    List<Agent> findByType(String type);
    List<Agent> findByCapability(String capability);
    List<Agent> findByLastHealthCheckBefore(LocalDateTime timestamp);
    Optional<Agent> findByName(String name);
    long countByStatus(AgentStatus status);
}
