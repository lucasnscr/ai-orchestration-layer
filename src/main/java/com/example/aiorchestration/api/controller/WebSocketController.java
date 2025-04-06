package com.example.aiorchestration.api.controller;

import com.example.aiorchestration.agent.event.AgentEvent;
import com.example.aiorchestration.workflow.event.WorkflowEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Handle agent events and broadcast to subscribers
     * 
     * @param event The agent event
     */
    public void handleAgentEvent(AgentEvent event) {
        log.info("Broadcasting agent event: {}", event.getType());
        messagingTemplate.convertAndSend("/topic/agents/" + event.getAgentId(), event);
        messagingTemplate.convertAndSend("/topic/agents", event);
    }
    
    /**
     * Handle workflow events and broadcast to subscribers
     * 
     * @param event The workflow event
     */
    public void handleWorkflowEvent(WorkflowEvent event) {
        log.info("Broadcasting workflow event: {}", event.getType());
        messagingTemplate.convertAndSend("/topic/workflows/" + event.getWorkflowId(), event);
        messagingTemplate.convertAndSend("/topic/workflows", event);
        
        if (event.getExecutionId() != null) {
            messagingTemplate.convertAndSend("/topic/executions/" + event.getExecutionId(), event);
        }
    }
    
    /**
     * Echo message for testing WebSocket connection
     * 
     * @param message The message to echo
     * @return The echoed message
     */
    @MessageMapping("/echo")
    @SendTo("/topic/echo")
    public String echo(String message) {
        log.info("Received echo message: {}", message);
        return "Echo: " + message;
    }
}
