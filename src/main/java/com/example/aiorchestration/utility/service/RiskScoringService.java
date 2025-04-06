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
public class RiskScoringService {

    private final AuditLogService auditLogService;
    
    /**
     * Calculate risk score based on input parameters
     * 
     * @param parameters Map of parameters to consider for risk scoring
     * @param userId User ID for audit logging
     * @return Risk score between 0 and 100
     */
    @Cacheable(value = "riskScores", key = "#parameters.toString()")
    public int calculateRiskScore(Map<String, Object> parameters, String userId) {
        log.info("Calculating risk score for parameters: {}", parameters);
        
        // For MVP, implement a simple scoring algorithm
        // In a real implementation, this would use a more sophisticated model
        int baseScore = 50;
        int adjustedScore = baseScore;
        
        // Example risk factors
        if (parameters.containsKey("amount")) {
            double amount = Double.parseDouble(parameters.get("amount").toString());
            if (amount > 10000) {
                adjustedScore += 20;
            } else if (amount > 5000) {
                adjustedScore += 10;
            } else if (amount > 1000) {
                adjustedScore += 5;
            }
        }
        
        if (parameters.containsKey("country")) {
            String country = parameters.get("country").toString();
            if (isHighRiskCountry(country)) {
                adjustedScore += 15;
            }
        }
        
        if (parameters.containsKey("customerAge")) {
            int customerAge = Integer.parseInt(parameters.get("customerAge").toString());
            if (customerAge < 25) {
                adjustedScore += 5;
            }
        }
        
        if (parameters.containsKey("newCustomer")) {
            boolean newCustomer = Boolean.parseBoolean(parameters.get("newCustomer").toString());
            if (newCustomer) {
                adjustedScore += 10;
            }
        }
        
        // Ensure score is between 0 and 100
        int finalScore = Math.max(0, Math.min(100, adjustedScore));
        
        // Log the risk score calculation
        Map<String, Object> auditData = new HashMap<>(parameters);
        auditData.put("riskScore", finalScore);
        auditLogService.logEvent("RISK_SCORE_CALCULATION", null, "RISK", userId, auditData);
        
        return finalScore;
    }
    
    /**
     * Determine if a transaction should be flagged for review based on risk score
     * 
     * @param riskScore The calculated risk score
     * @param thresholdService The threshold service to use
     * @param context The context for threshold determination
     * @return True if the transaction should be flagged, false otherwise
     */
    public boolean shouldFlagForReview(int riskScore, ThresholdService thresholdService, String context) {
        int threshold = thresholdService.getThreshold(context);
        return riskScore >= threshold;
    }
    
    /**
     * Check if a country is considered high risk
     * 
     * @param country Country code or name
     * @return True if the country is high risk, false otherwise
     */
    private boolean isHighRiskCountry(String country) {
        // For MVP, use a simple list of high-risk countries
        // In a real implementation, this would use a more comprehensive data source
        String[] highRiskCountries = {"XYZ", "ABC", "123"};
        
        for (String highRiskCountry : highRiskCountries) {
            if (highRiskCountry.equalsIgnoreCase(country)) {
                return true;
            }
        }
        
        return false;
    }
}
