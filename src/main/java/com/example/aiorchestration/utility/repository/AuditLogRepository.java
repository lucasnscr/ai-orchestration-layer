package com.example.aiorchestration.utility.repository;

import com.example.aiorchestration.utility.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
    List<AuditLog> findByType(String type);
    List<AuditLog> findByEntityIdAndEntityType(String entityId, String entityType);
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<AuditLog> findByUserId(String userId);
}
