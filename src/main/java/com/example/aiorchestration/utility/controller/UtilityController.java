package com.example.aiorchestration.utility.controller;

import com.example.aiorchestration.utility.model.AuditLog;
import com.example.aiorchestration.utility.service.AuditLogService;
import com.example.aiorchestration.utility.service.RiskScoringService;
import com.example.aiorchestration.utility.service.ThresholdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/utility")
@RequiredArgsConstructor
@Slf4j
public class UtilityController {

    private final AuditLogService auditLogService;
    private final RiskScoringService riskScoringService;
    private final ThresholdService thresholdService;
    
    @GetMapping("/audit-logs/type/{type}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByType(@PathVariable String type) {
        log.info("Getting audit logs by type: {}", type);
        return ResponseEntity.ok(auditLogService.getAuditLogsByType(type));
    }
    
    @GetMapping("/audit-logs/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByEntity(
            @PathVariable String entityType, 
            @PathVariable String entityId) {
        log.info("Getting audit logs by entity: {}/{}", entityType, entityId);
        return ResponseEntity.ok(auditLogService.getAuditLogsByEntity(entityId, entityType));
    }
    
    @GetMapping("/audit-logs/time-range")
    public ResponseEntity<List<AuditLog>> getAuditLogsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("Getting audit logs by time range: {} to {}", start, end);
        return ResponseEntity.ok(auditLogService.getAuditLogsByTimeRange(start, end));
    }
    
    @GetMapping("/audit-logs/user/{userId}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByUser(@PathVariable String userId) {
        log.info("Getting audit logs by user: {}", userId);
        return ResponseEntity.ok(auditLogService.getAuditLogsByUser(userId));
    }
    
    @PostMapping("/risk-score")
    public ResponseEntity<Integer> calculateRiskScore(
            @RequestBody Map<String, Object> parameters,
            Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : "anonymous";
        log.info("Calculating risk score for parameters: {}, user: {}", parameters, userId);
        return ResponseEntity.ok(riskScoringService.calculateRiskScore(parameters, userId));
    }
    
    @GetMapping("/threshold/{context}")
    public ResponseEntity<Integer> getThreshold(@PathVariable String context) {
        log.info("Getting threshold for context: {}", context);
        return ResponseEntity.ok(thresholdService.getThreshold(context));
    }
    
    @PutMapping("/threshold/{context}")
    public ResponseEntity<Void> updateThreshold(
            @PathVariable String context,
            @RequestParam int value,
            Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : "anonymous";
        log.info("Updating threshold for context: {} to value: {}, user: {}", context, value, userId);
        thresholdService.updateThreshold(context, value, userId);
        return ResponseEntity.ok().build();
    }
}
