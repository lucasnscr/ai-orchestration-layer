package com.example.aiorchestration.workflow.repository;

import com.example.aiorchestration.workflow.model.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, String> {
    Optional<Workflow> findByName(String name);
    List<Workflow> findByType(String type);
}
