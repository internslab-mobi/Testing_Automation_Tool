package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mobi.testingautomationtool.entity.TestingAuditLog;

public interface AuditLogRepository extends JpaRepository<TestingAuditLog, Integer> {
}
