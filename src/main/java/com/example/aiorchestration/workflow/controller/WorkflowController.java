package com.example.aiorchestration.workflow.controller;

import com.example.aiorchestration.workflow.model.Workflow;
import com.example.aiorchestration.workflow.model.WorkflowExecution;
import com.example.aiorchestration.workflow.service.WorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
@Slf4j
public class WorkflowController {

    private final WorkflowService workflowService;
    
    @PostMapping
    public ResponseEntity<Workflow> createWorkflow(@Valid @RequestBody Workflow workflow) {
        log.info("Creating workflow: {}", workflow.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(workflowService.createWorkflow(workflow));
    }
    
    @GetMapping
    public ResponseEntity<List<Workflow>> getAllWorkflows() {
        log.info("Getting all workflows");
        return ResponseEntity.ok(workflowService.getAllWorkflows());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Workflow> getWorkflowById(@PathVariable String id) {
        log.info("Getting workflow by id: {}", id);
        return ResponseEntity.ok(workflowService.getWorkflowById(id));
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Workflow>> getWorkflowsByType(@PathVariable String type) {
        log.info("Getting workflows by type: {}", type);
        return ResponseEntity.ok(workflowService.getWorkflowsByType(type));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Workflow> updateWorkflow(@PathVariable String id, @Valid @RequestBody Workflow workflow) {
        log.info("Updating workflow: {}", id);
        return ResponseEntity.ok(workflowService.updateWorkflow(id, workflow));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkflow(@PathVariable String id) {
        log.info("Deleting workflow: {}", id);
        workflowService.deleteWorkflow(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/execute")
    public ResponseEntity<CompletableFuture<WorkflowExecution>> executeWorkflow(
            @PathVariable String id, 
            @RequestBody(required = false) Map<String, Object> inputs) {
        log.info("Executing workflow: {}", id);
        return ResponseEntity.accepted().body(workflowService.executeWorkflow(id, inputs));
    }
    
    @GetMapping("/{id}/executions")
    public ResponseEntity<List<WorkflowExecution>> getWorkflowExecutions(@PathVariable String id) {
        log.info("Getting executions for workflow: {}", id);
        return ResponseEntity.ok(workflowService.getWorkflowExecutions(id));
    }
    
    @GetMapping("/executions/{executionId}")
    public ResponseEntity<WorkflowExecution> getWorkflowExecution(@PathVariable String executionId) {
        log.info("Getting workflow execution: {}", executionId);
        return ResponseEntity.ok(workflowService.getWorkflowExecution(executionId));
    }
    
    @PostMapping("/executions/{executionId}/cancel")
    public ResponseEntity<Void> cancelWorkflowExecution(@PathVariable String executionId) {
        log.info("Cancelling workflow execution: {}", executionId);
        workflowService.cancelWorkflowExecution(executionId);
        return ResponseEntity.accepted().build();
    }
    
    @PostMapping("/executions/{executionId}/steps/{stepId}/human-review")
    public ResponseEntity<Void> completeHumanReview(
            @PathVariable String executionId,
            @PathVariable String stepId,
            @RequestParam boolean approved,
            @RequestBody String result) {
        log.info("Completing human review for execution: {}, step: {}, approved: {}", 
                executionId, stepId, approved);
        workflowService.completeHumanReview(executionId, stepId, result, approved);
        return ResponseEntity.accepted().build();
    }
}
