package com.example.aiorchestration.workflow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "workflow_steps")
public class WorkflowStep {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(nullable = false)
    private int sequence;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StepType type;
    
    private String agentId;
    
    @Column(length = 2000)
    private String prompt;
    
    @Column(length = 2000)
    private String condition;
    
    private String nextStepOnSuccess;
    
    private String nextStepOnFailure;
    
    @Column(nullable = false)
    private boolean required;
    
    private int retryCount;
    
    private long retryDelayMs;
}
