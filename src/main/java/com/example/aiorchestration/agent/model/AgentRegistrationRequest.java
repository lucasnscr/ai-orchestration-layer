package com.example.aiorchestration.agent.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class AgentRegistrationRequest {
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    private String name;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @NotBlank(message = "Type is required")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Type can only contain alphanumeric characters, underscores, and hyphens")
    private String type;
    
    @NotBlank(message = "Capability is required")
    private String capability;
    
    private Map<String, String> metadata = new HashMap<>();
}
