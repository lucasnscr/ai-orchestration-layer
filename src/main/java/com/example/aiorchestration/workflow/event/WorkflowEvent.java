package com.example.aiorchestration.workflow.event;

import com.example.aiorchestration.workflow.model.WorkflowExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowEvent {
    private WorkflowEventType type;
    private String workflowId;
    private String workflowName;
    private String executionId;
    private WorkflowExecutionStatus status;
    private String stepId;
    private String stepName;
    private LocalDateTime timestamp;
}
