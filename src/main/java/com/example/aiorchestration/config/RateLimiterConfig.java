package com.example.aiorchestration.config;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimiterConfig {
    
    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig apiConfig = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .limitForPeriod(100)
                .timeoutDuration(Duration.ofMillis(100))
                .build();
        
        RateLimiterConfig agentExecutionConfig = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .limitForPeriod(20)
                .timeoutDuration(Duration.ofMillis(500))
                .build();
        
        RateLimiterRegistry registry = RateLimiterRegistry.of(RateLimiterConfig.custom().build());
        registry.addConfiguration("api", apiConfig);
        registry.addConfiguration("agentExecution", agentExecutionConfig);
        
        return registry;
    }
}
