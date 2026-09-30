package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.mobi.testingautomationtool.entity.TestingAuditLog;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<TestingAuditLog, Integer> {
    List<TestingAuditLog> findByTestCase_TestcaseIdOrderByExecutedAtDesc(Integer testcaseId);
}
