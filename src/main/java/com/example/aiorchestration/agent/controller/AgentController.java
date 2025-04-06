package com.example.aiorchestration.agent.controller;

import com.example.aiorchestration.agent.model.*;
import com.example.aiorchestration.agent.service.AgentManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
@Slf4j
public class AgentController {

    private final AgentManagementService agentService;
    
    @PostMapping
    public ResponseEntity<Agent> registerAgent(@Valid @RequestBody AgentRegistrationRequest request) {
        log.info("Registering agent: {}", request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(agentService.registerAgent(request));
    }
    
    @GetMapping
    public ResponseEntity<List<Agent>> getAllAgents() {
        log.info("Getting all agents");
        return ResponseEntity.ok(agentService.getAllAgents());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Agent> getAgentById(@PathVariable String id) {
        log.info("Getting agent by id: {}", id);
        return ResponseEntity.ok(agentService.getAgentById(id));
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Agent>> getAgentsByStatus(@PathVariable AgentStatus status) {
        log.info("Getting agents by status: {}", status);
        return ResponseEntity.ok(agentService.getAgentsByStatus(status));
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Agent>> getAgentsByType(@PathVariable String type) {
        log.info("Getting agents by type: {}", type);
        return ResponseEntity.ok(agentService.getAgentsByType(type));
    }
    
    @GetMapping("/capability/{capability}")
    public ResponseEntity<List<Agent>> getAgentsByCapability(@PathVariable String capability) {
        log.info("Getting agents by capability: {}", capability);
        return ResponseEntity.ok(agentService.getAgentsByCapability(capability));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Agent> updateAgent(@PathVariable String id, @Valid @RequestBody AgentRegistrationRequest request) {
        log.info("Updating agent: {}", id);
        return ResponseEntity.ok(agentService.updateAgent(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgent(@PathVariable String id) {
        log.info("Deleting agent: {}", id);
        agentService.deleteAgent(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<Agent> updateAgentStatus(@PathVariable String id, @RequestBody AgentStatus status) {
        log.info("Updating agent status: {} to {}", id, status);
        return ResponseEntity.ok(agentService.updateAgentStatus(id, status));
    }
    
    @PostMapping("/{id}/execute")
    public ResponseEntity<CompletableFuture<AgentResponse>> executeAgent(@PathVariable String id, @RequestBody AgentRequest request) {
        log.info("Executing agent: {}", id);
        return ResponseEntity.accepted().body(agentService.executeAgent(id, request));
    }
    
    @PostMapping("/{id}/health-check")
    public ResponseEntity<Void> performHealthCheck(@PathVariable String id) {
        log.info("Performing health check for agent: {}", id);
        agentService.performHealthCheck(id);
        return ResponseEntity.ok().build();
    }
}
