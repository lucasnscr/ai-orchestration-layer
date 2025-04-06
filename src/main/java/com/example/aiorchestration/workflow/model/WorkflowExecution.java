package com.example.aiorchestration.workflow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "workflow_executions")
public class WorkflowExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String workflowId;
    
    @Column(nullable = false)
    private String workflowName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowExecutionStatus status;
    
    private String currentStepId;
    
    @ElementCollection
    @CollectionTable(name = "workflow_execution_results", 
                    joinColumns = @JoinColumn(name = "execution_id"))
    @MapKeyColumn(name = "step_id")
    @Column(name = "result", length = 10000)
    private Map<String, String> stepResults = new HashMap<>();
    
    @ElementCollection
    @CollectionTable(name = "workflow_execution_metadata", 
                    joinColumns = @JoinColumn(name = "execution_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata = new HashMap<>();
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String errorMessage;
    
    @PrePersist
    protected void onCreate() {
        startTime = LocalDateTime.now();
    }
}
