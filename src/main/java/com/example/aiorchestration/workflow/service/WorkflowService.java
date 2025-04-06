package com.example.aiorchestration.workflow.service;

import com.example.aiorchestration.agent.model.AgentRequest;
import com.example.aiorchestration.agent.model.AgentResponse;
import com.example.aiorchestration.agent.service.AgentManagementService;
import com.example.aiorchestration.workflow.event.WorkflowEvent;
import com.example.aiorchestration.workflow.event.WorkflowEventType;
import com.example.aiorchestration.workflow.model.*;
import com.example.aiorchestration.workflow.repository.WorkflowExecutionRepository;
import com.example.aiorchestration.workflow.repository.WorkflowRepository;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final AgentManagementService agentService;
    private final RabbitTemplate rabbitTemplate;
    
    @Transactional
    public Workflow createWorkflow(Workflow workflow) {
        // Check if workflow with same name already exists
        if (workflowRepository.findByName(workflow.getName()).isPresent()) {
            throw new IllegalArgumentException("Workflow with name " + workflow.getName() + " already exists");
        }
        
        // Save workflow
        Workflow savedWorkflow = workflowRepository.save(workflow);
        
        // Publish workflow created event
        publishWorkflowEvent(WorkflowEventType.CREATED, savedWorkflow, null, null);
        
        return savedWorkflow;
    }
    
    @Transactional(readOnly = true)
    public List<Workflow> getAllWorkflows() {
        return workflowRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Workflow getWorkflowById(String id) {
        return workflowRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found with id: " + id));
    }
    
    @Transactional(readOnly = true)
    public List<Workflow> getWorkflowsByType(String type) {
        return workflowRepository.findByType(type);
    }
    
    @Transactional
    public Workflow updateWorkflow(String id, Workflow workflow) {
        Workflow existingWorkflow = getWorkflowById(id);
        
        // Check if name is being changed and if new name already exists
        if (!existingWorkflow.getName().equals(workflow.getName()) && 
            workflowRepository.findByName(workflow.getName()).isPresent()) {
            throw new IllegalArgumentException("Workflow with name " + workflow.getName() + " already exists");
        }
        
        // Update workflow
        existingWorkflow.setName(workflow.getName());
        existingWorkflow.setDescription(workflow.getDescription());
        existingWorkflow.setType(workflow.getType());
        existingWorkflow.setSteps(workflow.getSteps());
        existingWorkflow.setMetadata(workflow.getMetadata());
        
        // Save workflow
        Workflow updatedWorkflow = workflowRepository.save(existingWorkflow);
        
        // Publish workflow updated event
        publishWorkflowEvent(WorkflowEventType.UPDATED, updatedWorkflow, null, null);
        
        return updatedWorkflow;
    }
    
    @Transactional
    public void deleteWorkflow(String id) {
        Workflow workflow = getWorkflowById(id);
        
        // Delete workflow
        workflowRepository.delete(workflow);
        
        // Publish workflow deleted event
        publishWorkflowEvent(WorkflowEventType.DELETED, workflow, null, null);
    }
    
    @Async
    @Transactional
    public CompletableFuture<WorkflowExecution> executeWorkflow(String id, Map<String, Object> inputs) {
        Workflow workflow = getWorkflowById(id);
        
        // Create workflow execution
        WorkflowExecution execution = WorkflowExecution.builder()
                .workflowId(workflow.getId())
                .workflowName(workflow.getName())
                .status(WorkflowExecutionStatus.PENDING)
                .metadata(new HashMap<>())
                .stepResults(new HashMap<>())
                .build();
        
        // Add inputs to metadata
        if (inputs != null) {
            inputs.forEach((key, value) -> execution.getMetadata().put(key, value.toString()));
        }
        
        // Save execution
        WorkflowExecution savedExecution = workflowExecutionRepository.save(execution);
        
        // Start execution asynchronously
        CompletableFuture.runAsync(() -> startWorkflowExecution(savedExecution.getId()));
        
        return CompletableFuture.completedFuture(savedExecution);
    }
    
    @Transactional
    public void startWorkflowExecution(String executionId) {
        WorkflowExecution execution = workflowExecutionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Workflow execution not found with id: " + executionId));
        
        Workflow workflow = workflowRepository.findById(execution.getWorkflowId())
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found with id: " + execution.getWorkflowId()));
        
        // Update status to RUNNING
        execution.setStatus(WorkflowExecutionStatus.RUNNING);
        execution.setStartTime(LocalDateTime.now());
        workflowExecutionRepository.save(execution);
        
        // Publish workflow execution started event
        publishWorkflowEvent(WorkflowEventType.EXECUTION_STARTED, workflow, execution, null);
        
        try {
            // Find first step
            Optional<WorkflowStep> firstStep = workflow.getSteps().stream()
                    .filter(step -> step.getSequence() == 1)
                    .findFirst();
            
            if (firstStep.isPresent()) {
                // Execute first step
                executeWorkflowStep(execution.getId(), firstStep.get().getId());
            } else {
                // No steps to execute
                completeWorkflowExecution(execution.getId(), "No steps to execute");
            }
        } catch (Exception e) {
            // Handle exception
            failWorkflowExecution(execution.getId(), "Error starting workflow execution: " + e.getMessage());
        }
    }
    
    @Transactional
    @Retry(name = "workflowStepExecution")
    public void executeWorkflowStep(String executionId, String stepId) {
        WorkflowExecution execution = workflowExecutionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Workflow execution not found with id: " + executionId));
        
        Workflow workflow = workflowRepository.findById(execution.getWorkflowId())
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found with id: " + execution.getWorkflowId()));
        
        // Find step
        WorkflowStep step = workflow.getSteps().stream()
                .filter(s -> s.getId().equals(stepId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Workflow step not found with id: " + stepId));
        
        // Update current step
        execution.setCurrentStepId(stepId);
        workflowExecutionRepository.save(execution);
        
        // Publish step started event
        publishWorkflowEvent(WorkflowEventType.STEP_STARTED, workflow, execution, step);
        
        try {
            String result = "";
            
            // Execute step based on type
            switch (step.getType()) {
                case AGENT_EXECUTION:
                    result = executeAgentStep(execution, step);
                    break;
                case CONDITION:
                    result = executeConditionStep(execution, step);
                    break;
                case HUMAN_REVIEW:
                    requestHumanReview(execution, step);
                    return; // Return early, will be continued when human review is completed
                case WAIT:
                    // Implement wait logic
                    result = "Wait completed";
                    break;
            }
            
            // Store step result
            execution.getStepResults().put(stepId, result);
            workflowExecutionRepository.save(execution);
            
            // Publish step completed event
            publishWorkflowEvent(WorkflowEventType.STEP_COMPLETED, workflow, execution, step);
            
            // Find next step
            String nextStepId = findNextStep(workflow, step, true);
            
            if (nextStepId != null) {
                // Execute next step
                executeWorkflowStep(executionId, nextStepId);
            } else {
                // No more steps, complete workflow
                completeWorkflowExecution(executionId, "Workflow completed successfully");
            }
        } catch (Exception e) {
            log.error("Error executing workflow step: {}", e.getMessage(), e);
            
            // Publish step failed event
            publishWorkflowEvent(WorkflowEventType.STEP_FAILED, workflow, execution, step);
            
            // Check if retry is possible
            if (step.getRetryCount() > 0) {
                // Decrement retry count
                step.setRetryCount(step.getRetryCount() - 1);
                
                // Schedule retry
                // In a real implementation, this would use a scheduler or delay queue
                // For simplicity, we'll just retry immediately
                executeWorkflowStep(executionId, stepId);
            } else {
                // Find next step on failure
                String nextStepId = findNextStep(workflow, step, false);
                
                if (nextStepId != null) {
                    // Execute next step
                    executeWorkflowStep(executionId, nextStepId);
                } else {
                    // No more steps, fail workflow if step is required
                    if (step.isRequired()) {
                        failWorkflowExecution(executionId, "Required step failed: " + step.getName() + " - " + e.getMessage());
                    } else {
                        completeWorkflowExecution(executionId, "Workflow completed with non-required step failure");
                    }
                }
            }
        }
    }
    
    private String executeAgentStep(WorkflowExecution execution, WorkflowStep step) throws Exception {
        if (step.getAgentId() == null) {
            throw new IllegalArgumentException("Agent ID is required for agent execution step");
        }
        
        // Create agent request
        AgentRequest agentRequest = AgentRequest.builder()
                .prompt(step.getPrompt())
                .build();
        
        // Add workflow context to parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("workflowId", execution.getWorkflowId());
        parameters.put("executionId", execution.getId());
        parameters.put("stepResults", execution.getStepResults());
        parameters.put("metadata", execution.getMetadata());
        agentRequest.setParameters(parameters);
        
        // Execute agent
        AgentResponse response = agentService.executeAgent(step.getAgentId(), agentRequest).get();
        
        if (!response.isSuccess()) {
            throw new RuntimeException("Agent execution failed: " + response.getErrorMessage());
        }
        
        return response.getResult();
    }
    
    private String executeConditionStep(WorkflowExecution execution, WorkflowStep step) {
        if (step.getCondition() == null) {
            throw new IllegalArgumentException("Condition is required for condition step");
        }
        
        // In a real implementation, this would evaluate the condition using a script engine
        // For simplicity, we'll just return a fixed result
        return "Condition evaluated to true";
    }
    
    private void requestHumanReview(WorkflowExecution execution, WorkflowStep step) {
        // Update status to WAITING_FOR_HUMAN
        execution.setStatus(WorkflowExecutionStatus.WAITING_FOR_HUMAN);
        workflowExecutionRepository.save(execution);
        
        // Publish human review requested event
        Workflow workflow = workflowRepository.findById(execution.getWorkflowId())
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found with id: " + execution.getWorkflowId()));
        
        publishWorkflowEvent(WorkflowEventType.HUMAN_REVIEW_REQUESTED, workflow, execution, step);
    }
    
    @Transactional
    public void completeHumanReview(String executionId, String stepId, String result, boolean approved) {
        WorkflowExecution execution = workflowExecutionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Workflow execution not found with id: " + executionId));
        
        Workflow workflow = workflowRepository.findById(execution.getWorkflowId())
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found with id: " + execution.getWorkflowId()));
        
        // Find step
        WorkflowStep step = workflow.getSteps().stream()
                .filter(s -> s.getId().equals(stepId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Workflow step not found with id: " + stepId));
        
        // Store step result
        execution.getStepResults().put(stepId, result);
        
        // Update status to RUNNING
        execution.setStatus(WorkflowExecutionStatus.RUNNING);
        workflowExecutionRepository.save(execution);
        
        // Publish human review completed event
        publishWorkflowEvent(WorkflowEventType.HUMAN_REVIEW_COMPLETED, workflow, execution, step);
        
        // Find next step based on approval
        String nextStepId = findNextStep(workflow, step, approved);
        
        if (nextStepId != null) {
            // Execute next step
            executeWorkflowStep(executionId, nextStepId);
        } else {
            // No more steps, complete workflow
            completeWorkflowExecution(executionId, "Workflow completed after human review");
        }
    }
    
    private String findNextStep(Workflow workflow, WorkflowStep currentStep, boolean success) {
        // Check if explicit next step is defined
        if (success && currentStep.getNextStepOnSuccess() != null) {
            return currentStep.getNextStepOnSuccess();
        } else if (!success && currentStep.getNextStepOnFailure() != null) {
            return currentStep.getNextStepOnFailure();
        }
        
        // Find next step by sequence
        return workflow.getSteps().stream()
                .filter(step -> step.getSequence() == currentStep.getSequence() + 1)
                .map(WorkflowStep::getId)
                .findFirst()
                .orElse(null);
    }
    
    @Transactional
    public void completeWorkflowExecution(String executionId, String message) {
        WorkflowExecution execution = workflowExecutionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Workflow execution not found with id: " + executionId));
        
        // Update status to COMPLETED
        execution.setStatus(WorkflowExecutionStatus.COMPLETED);
        execution.setEndTime(LocalDateTime.now());
        workflowExecutionRepository.save(execution);
        
        // Publish workflow execution completed event
        Workflow workflow = workflowRepository.findById(execution.getWorkflowId())
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found with id: " + execution.getWorkflowId()));
        
        publishWorkflowEvent(WorkflowEventType.EXECUTION_COMPLETED, workflow, execution, null);
    }
    
    @Transactional
    public void failWorkflowExecution(String executionId, String errorMessage) {
        WorkflowExecution execution = workflowExecutionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Workflow execution not found with id: " + executionId));
        
        // Update status to FAILED
        execution.setStatus(WorkflowExecutionStatus.FAILED);
        execution.setEndTime(LocalDateTime.now());
        execution.setErrorMessage(errorMessage);
        workflowExecutionRepository.save(execution);
        
        // Publish workflow execution failed event
        Workflow workflow = workflowRepository.findById(execution.getWorkflowId())
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found with id: " + execution.getWorkflowId()));
        
        publishWorkflowEvent(WorkflowEventType.EXECUTION_FAILED, workflow, execution, null);
    }
    
    @Transactional
    public void cancelWorkflowExecution(String executionId) {
        WorkflowExecution execution = workflowExecutionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Workflow execution not found with id: " + executionId));
        
        // Update status to CANCELLED
        execution.setStatus(WorkflowExecutionStatus.CANCELLED);
        execution.setEndTime(LocalDateTime.now());
        workflowExecutionRepository.save(execution);
        
        // Publish workflow execution failed event
        Workflow workflow = workflowRepository.findById(execution.getWorkflowId())
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found with id: " + execution.getWorkflowId()));
        
        publishWorkflowEvent(WorkflowEventType.EXECUTION_FAILED, workflow, execution, null);
    }
    
    @Transactional(readOnly = true)
    public List<WorkflowExecution> getWorkflowExecutions(String workflowId) {
        return workflowExecutionRepository.findByWorkflowId(workflowId);
    }
    
    @Transactional(readOnly = true)
    public WorkflowExecution getWorkflowExecution(String executionId) {
        return workflowExecutionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Workflow execution not found with id: " + executionId));
    }
    
    private void publishWorkflowEvent(WorkflowEventType eventType, Workflow workflow, WorkflowExecution execution, WorkflowStep step) {
        WorkflowEvent event = WorkflowEvent.builder()
                .type(eventType)
                .workflowId(workflow.getId())
                .workflowName(workflow.getName())
                .executionId(execution != null ? execution.getId() : null)
                .status(execution != null ? execution.getStatus() : null)
                .stepId(step != null ? step.getId() : null)
                .stepName(step != null ? step.getName() : null)
                .timestamp(LocalDateTime.now())
                .build();
        
        rabbitTemplate.convertAndSend("workflow-events", event);
    }
}
