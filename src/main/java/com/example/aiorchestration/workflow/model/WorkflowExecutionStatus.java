package com.example.aiorchestration.workflow.model;

public enum WorkflowExecutionStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED,
    WAITING_FOR_HUMAN
}
