package com.example.aiorchestration.agent.service;

import com.example.aiorchestration.agent.event.AgentEvent;
import com.example.aiorchestration.agent.event.AgentEventType;
import com.example.aiorchestration.agent.model.*;
import com.example.aiorchestration.agent.repository.AgentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class AgentManagementService {

    private final AgentRepository agentRepository;
    private final ChatClient chatClient;
    private final RabbitTemplate rabbitTemplate;

    AgentManagementService(AgentRepository agentRepository,
                           ChatClient.Builder chatClient,
                           RabbitTemplate rabbitTemplate) {
        this.agentRepository = agentRepository;
        this.chatClient = chatClient.build();
        this.rabbitTemplate = rabbitTemplate;
    }
    
    @Transactional
    public Agent registerAgent(AgentRegistrationRequest request) {
        // Check if agent with same name already exists
        if (agentRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Agent with name " + request.getName() + " already exists");
        }
        
        // Create new agent
        Agent agent = Agent.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .capability(request.getCapability())
                .metadata(request.getMetadata())
                .status(AgentStatus.IDLE)
                .build();
        
        // Save agent
        Agent savedAgent = agentRepository.save(agent);
        
        // Publish agent registered event
        publishAgentEvent(AgentEventType.REGISTERED, savedAgent);
        
        return savedAgent;
    }
    
    @Transactional(readOnly = true)
    public List<Agent> getAllAgents() {
        return agentRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Agent getAgentById(String id) {
        return agentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agent not found with id: " + id));
    }
    
    @Transactional(readOnly = true)
    public List<Agent> getAgentsByStatus(AgentStatus status) {
        return agentRepository.findByStatus(status);
    }
    
    @Transactional(readOnly = true)
    public List<Agent> getAgentsByType(String type) {
        return agentRepository.findByType(type);
    }
    
    @Transactional(readOnly = true)
    public List<Agent> getAgentsByCapability(String capability) {
        return agentRepository.findByCapability(capability);
    }
    
    @Transactional
    public Agent updateAgent(String id, AgentRegistrationRequest request) {
        Agent agent = getAgentById(id);
        
        // Check if name is being changed and if new name already exists
        if (!agent.getName().equals(request.getName()) && 
            agentRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Agent with name " + request.getName() + " already exists");
        }
        
        // Update agent
        agent.setName(request.getName());
        agent.setDescription(request.getDescription());
        agent.setType(request.getType());
        agent.setCapability(request.getCapability());
        agent.setMetadata(request.getMetadata());
        
        // Save agent
        Agent updatedAgent = agentRepository.save(agent);
        
        // Publish agent updated event
        publishAgentEvent(AgentEventType.UPDATED, updatedAgent);
        
        return updatedAgent;
    }
    
    @Transactional
    public void deleteAgent(String id) {
        Agent agent = getAgentById(id);
        
        // Delete agent
        agentRepository.delete(agent);
        
        // Publish agent deleted event
        publishAgentEvent(AgentEventType.DELETED, agent);
    }
    
    @Transactional
    public Agent updateAgentStatus(String id, AgentStatus status) {
        Agent agent = getAgentById(id);
        
        // Update status
        agent.setStatus(status);
        
        // Save agent
        Agent updatedAgent = agentRepository.save(agent);
        
        // Publish agent status changed event
        publishAgentEvent(AgentEventType.STATUS_CHANGED, updatedAgent);
        
        return updatedAgent;
    }
    
    @Async
    @Transactional
    @CircuitBreaker(name = "agentExecution", fallbackMethod = "executeAgentFallback")
    @Retry(name = "agentExecution")
    public CompletableFuture<AgentResponse> executeAgent(String id, AgentRequest request) {
        Agent agent = getAgentById(id);
        
        // Update agent status to WORKING
        updateAgentStatus(id, AgentStatus.WORKING);
        
        // Publish agent execution started event
        publishAgentEvent(AgentEventType.EXECUTION_STARTED, agent);
        
        try {
            // Create message for AI model
            Message userMessage = new UserMessage(request.getPrompt());
            Prompt prompt = new Prompt(userMessage);
            
            // Execute AI model
            ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
            
            // Create agent response
            AgentResponse agentResponse = AgentResponse.builder()
                    .agentId(id)
                    .result(Objects.requireNonNull(response).getResult().getOutput().getText())
                    .success(true)
                    .build();
            
            // Update agent status to COMPLETE
            updateAgentStatus(id, AgentStatus.COMPLETE);
            
            // Publish agent execution completed event
            publishAgentEvent(AgentEventType.EXECUTION_COMPLETED, agent);
            
            return CompletableFuture.completedFuture(agentResponse);
        } catch (Exception e) {
            log.error("Error executing agent: {}", e.getMessage(), e);
            
            // Update agent status to ERROR
            updateAgentStatus(id, AgentStatus.ERROR);
            
            // Publish agent execution failed event
            publishAgentEvent(AgentEventType.EXECUTION_FAILED, agent);
            
            throw e;
        }
    }
    
    public CompletableFuture<AgentResponse> executeAgentFallback(String id, AgentRequest request, Throwable throwable) {
        log.error("Fallback for agent execution: {}", id, throwable);
        
        // Get agent
        Optional<Agent> agentOpt = agentRepository.findById(id);
        
        // Update agent status to ERROR if agent exists
        agentOpt.ifPresent(agent -> {
            agent.setStatus(AgentStatus.ERROR);
            agentRepository.save(agent);
            
            // Publish agent execution failed event
            publishAgentEvent(AgentEventType.EXECUTION_FAILED, agent);
        });
        
        // Create fallback response
        AgentResponse fallbackResponse = AgentResponse.builder()
                .agentId(id)
                .success(false)
                .errorMessage("Agent execution failed: " + throwable.getMessage())
                .result("I'm sorry, but I'm unable to process your request at the moment. Please try again later.")
                .build();
        
        return CompletableFuture.completedFuture(fallbackResponse);
    }
    
    @Transactional
    public void performHealthCheck(String id) {
        Agent agent = getAgentById(id);
        
        // Update last health check timestamp
        agent.setLastHealthCheck(LocalDateTime.now());
        
        // Save agent
        agentRepository.save(agent);
        
        // Publish agent health check event
        publishAgentEvent(AgentEventType.HEALTH_CHECK, agent);
    }
    
    private void publishAgentEvent(AgentEventType eventType, Agent agent) {
        AgentEvent event = AgentEvent.builder()
                .type(eventType)
                .agentId(agent.getId())
                .agentName(agent.getName())
                .status(agent.getStatus())
                .timestamp(LocalDateTime.now())
                .build();
        
        rabbitTemplate.convertAndSend("agent-events", event);
    }
}
