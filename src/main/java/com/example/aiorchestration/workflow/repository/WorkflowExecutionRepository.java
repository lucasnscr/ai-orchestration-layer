package com.example.aiorchestration.workflow.repository;

import com.example.aiorchestration.workflow.model.WorkflowExecution;
import com.example.aiorchestration.workflow.model.WorkflowExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, String> {
    List<WorkflowExecution> findByWorkflowId(String workflowId);
    List<WorkflowExecution> findByStatus(WorkflowExecutionStatus status);
    List<WorkflowExecution> findByWorkflowIdAndStatus(String workflowId, WorkflowExecutionStatus status);
    long countByStatus(WorkflowExecutionStatus status);
}
