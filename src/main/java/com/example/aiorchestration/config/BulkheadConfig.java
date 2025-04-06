package com.example.aiorchestration.config;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class BulkheadConfig {
    
    @Bean
    public BulkheadRegistry bulkheadRegistry() {
        BulkheadConfig agentExecutionConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(10)
                .maxWaitDuration(Duration.ofMillis(500))
                .build();
        
        BulkheadConfig workflowExecutionConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(5)
                .maxWaitDuration(Duration.ofSeconds(1))
                .build();
        
        BulkheadRegistry registry = BulkheadRegistry.of(BulkheadConfig.custom().build());
        registry.addConfiguration("agentExecution", agentExecutionConfig);
        registry.addConfiguration("workflowExecution", workflowExecutionConfig);
        
        return registry;
    }
}
