package com.example.aiorchestration.utility.service;

import com.example.aiorchestration.utility.model.AuditLog;
import com.example.aiorchestration.utility.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    
    @Transactional
    public AuditLog logEvent(String type, String entityId, String entityType, String userId, String data) {
        log.info("Logging audit event: type={}, entityId={}, entityType={}, userId={}", 
                type, entityId, entityType, userId);
        
        AuditLog auditLog = AuditLog.builder()
                .type(type)
                .entityId(entityId)
                .entityType(entityType)
                .userId(userId)
                .data(data)
                .build();
        
        return auditLogRepository.save(auditLog);
    }
    
    @Transactional
    public AuditLog logEvent(String type, String entityId, String entityType, String userId, Map<String, Object> data) {
        // Convert map to JSON string
        String dataJson = convertMapToJson(data);
        return logEvent(type, entityId, entityType, userId, dataJson);
    }
    
    @Transactional(readOnly = true)
    @Cacheable(value = "auditLogs", key = "#type")
    public List<AuditLog> getAuditLogsByType(String type) {
        return auditLogRepository.findByType(type);
    }
    
    @Transactional(readOnly = true)
    @Cacheable(value = "auditLogs", key = "#entityId + '-' + #entityType")
    public List<AuditLog> getAuditLogsByEntity(String entityId, String entityType) {
        return auditLogRepository.findByEntityIdAndEntityType(entityId, entityType);
    }
    
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByTimeRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByTimestampBetween(start, end);
    }
    
    @Transactional(readOnly = true)
    @Cacheable(value = "auditLogs", key = "#userId")
    public List<AuditLog> getAuditLogsByUser(String userId) {
        return auditLogRepository.findByUserId(userId);
    }
    
    private String convertMapToJson(Map<String, Object> data) {
        // Simple implementation for MVP
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":\"")
                .append(entry.getValue().toString().replace("\"", "\\\"")).append("\"");
            first = false;
        }
        
        json.append("}");
        return json.toString();
    }
}
