package com.example.aiorchestration.agent.event;

import com.example.aiorchestration.agent.model.AgentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentEvent {
    private AgentEventType type;
    private String agentId;
    private String agentName;
    private AgentStatus status;
    private LocalDateTime timestamp;
}
