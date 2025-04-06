package com.example.aiorchestration.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponse {
    private String agentId;
    private String result;
    private Map<String, Object> metadata = new HashMap<>();
    private boolean success;
    private String errorMessage;
}
