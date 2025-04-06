package com.example.aiorchestration.workflow.event;

public enum WorkflowEventType {
    CREATED,
    UPDATED,
    DELETED,
    EXECUTION_STARTED,
    EXECUTION_COMPLETED,
    EXECUTION_FAILED,
    STEP_STARTED,
    STEP_COMPLETED,
    STEP_FAILED,
    HUMAN_REVIEW_REQUESTED,
    HUMAN_REVIEW_COMPLETED
}
