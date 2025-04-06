package com.example.aiorchestration.utility.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThresholdService {

    private final AuditLogService auditLogService;
    
    /**
     * Get the threshold value for a specific context
     * 
     * @param context The context for which to get the threshold
     * @return The threshold value
     */
    @Cacheable(value = "thresholds", key = "#context")
    public int getThreshold(String context) {
        log.info("Getting threshold for context: {}", context);
        
        // For MVP, use hardcoded thresholds
        // In a real implementation, these would be configurable and stored in a database
        Map<String, Integer> thresholds = new HashMap<>();
        thresholds.put("default", 70);
        thresholds.put("high_value_transaction", 60);
        thresholds.put("new_customer", 65);
        thresholds.put("international_transfer", 55);
        
        // Log the threshold retrieval
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("context", context);
        auditData.put("threshold", thresholds.getOrDefault(context, thresholds.get("default")));
        auditLogService.logEvent("THRESHOLD_RETRIEVAL", null, "THRESHOLD", "system", auditData);
        
        return thresholds.getOrDefault(context, thresholds.get("default"));
    }
    
    /**
     * Update the threshold value for a specific context
     * 
     * @param context The context for which to update the threshold
     * @param value The new threshold value
     * @param userId User ID for audit logging
     */
    public void updateThreshold(String context, int value, String userId) {
        log.info("Updating threshold for context: {} to value: {}", context, value);
        
        // For MVP, this is a no-op since we're using hardcoded values
        // In a real implementation, this would update the database
        
        // Log the threshold update
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("context", context);
        auditData.put("newThreshold", value);
        auditLogService.logEvent("THRESHOLD_UPDATE", null, "THRESHOLD", userId, auditData);
    }
}
